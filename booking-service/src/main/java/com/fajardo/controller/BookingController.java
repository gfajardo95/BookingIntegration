package com.fajardo.controller;

import com.fajardo.model.Booking;
import com.fajardo.service.BookingBrokerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/booking")
public class BookingController {

    private final BookingBrokerService bookingBrokerService;

    public BookingController(@Autowired BookingBrokerService bookingBrokerService) {
        this.bookingBrokerService = bookingBrokerService;
    }

    @PostMapping()
    public boolean createBooking(@RequestBody Booking booking) {
        return bookingBrokerService.createBooking(booking);
    }
}
