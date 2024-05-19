package com.fajardo.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingConsumerService {

    @Value("${payments.producer-topic}")
    private String bookingDecisionsTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public BookingConsumerService(@Autowired KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${payments.consumer-topic}", groupId = "${payments.consumer-groupId}")
    public void consumeBookings(ConsumerRecord<String, String> booking) {
        System.out.println(booking.value());

        kafkaTemplate.send(bookingDecisionsTopic, booking.key(), "true");
    }
}
