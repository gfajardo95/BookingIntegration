package com.fajardo.controller;

import com.fajardo.model.Booking;
import com.fajardo.service.BookingQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/booking")
public class BookingController {

    private final BookingQueueService bookingQueueService;

    public BookingController(@Autowired BookingQueueService bookingQueueService) {
        this.bookingQueueService = bookingQueueService;
    }

    @PostMapping()
    public boolean createBooking(@RequestBody Booking booking) {
        return bookingQueueService.addBooking(booking);
    }
}
