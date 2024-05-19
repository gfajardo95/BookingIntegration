package com.fajardo.service;

import com.fajardo.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BookingBrokerService {

    @Value("${bookings.producer-topic}")
    private String bookingRequestsTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Map<String, BookingDecision> bookingDecisions = new ConcurrentHashMap<>();

    public BookingBrokerService(@Autowired KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean createBooking(Booking booking) {
        String bookingKey = UUID.randomUUID().toString();

        boolean isBookingProduced = produceBookingMessage(booking, bookingKey);
        if (!isBookingProduced) {
            return false;
        }

        Object bookingSignal = new Object();
        BookingDecision bookingDecision = new BookingDecision(bookingSignal, null);
        bookingDecisions.put(bookingKey, bookingDecision);

        synchronized (bookingSignal) {
            try {
                bookingSignal.wait();
            } catch (InterruptedException e) {
                return false;
            }
        }

        ConsumerRecord<String, String> response = bookingDecisions.get(bookingKey).getBookingDecision();
        // todo: process response

        return true;
    }

    @KafkaListener(topics = "${bookings.consumer-topic}", groupId = "${bookings.consumer-groupId}")
    public void consumeBookingDecisions(ConsumerRecord<String, String> bookingDecision) {
        if (bookingDecision != null) {
            bookingDecisions.get(bookingDecision.key()).setBookingDecision(bookingDecision);

            Object signal = bookingDecisions.get(bookingDecision.key()).getSignal();
            synchronized (signal) {
                signal.notify();
            }
        }
    }

    private boolean produceBookingMessage(Booking booking, String bookingKey) {
        AtomicBoolean producerResult = new AtomicBoolean(true);

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(bookingRequestsTopic, bookingKey, booking.getData());

        try {
            future
                    .completable()
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            producerResult.set(false);
                        }
                    });
        } catch (Exception e) {
            producerResult.set(false);
        }

        return producerResult.get();
    }

    @Data
    @AllArgsConstructor
    private static class BookingDecision {
        private Object signal;
        private ConsumerRecord<String, String> bookingDecision;
    }
}
