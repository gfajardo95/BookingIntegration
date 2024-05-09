package com.fajardo.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class BookingConsumerService {

    @KafkaListener(topics = "fajardo", groupId = "test")  // todo: get from properties
    public void consumeBookings(@Payload(required = false) String data) {
        System.out.println(data);
    }
}
