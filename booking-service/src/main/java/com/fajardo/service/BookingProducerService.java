package com.fajardo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class BookingProducerService {

    @Value("${booking-producer.topic}")
    private String bookingTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public BookingProducerService(@Autowired KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public ListenableFuture<SendResult<String, String>> produceBookingMessage(String data) {
        return kafkaTemplate.send(bookingTopic, data);
    }
}
