package com.fajardo.controller;

import com.fajardo.model.Booking;
import com.fajardo.service.BookingProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/v1/booking")
public class BookingController {

    private final BookingProducerService bookingProducerService;

    public BookingController(@Autowired BookingProducerService bookingProducerService) {
        this.bookingProducerService = bookingProducerService;
    }

    @PostMapping()
    public String sendBookingData(@RequestBody Booking booking) {
        ListenableFuture<SendResult<String, String>> future = bookingProducerService
                .produceBookingMessage(booking.getData());

        AtomicReference<String> response = new AtomicReference<>();
        try {
            future
                    .completable()
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            response.set(result.getProducerRecord().value());
                        } else {
                            response.set(ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            return e.getMessage();
        }

        return response.get();
    }
}
