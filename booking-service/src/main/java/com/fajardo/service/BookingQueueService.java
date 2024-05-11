package com.fajardo.service;

import com.fajardo.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class BookingQueueService {

    private final BookingProducerService bookingProducerService;
    private final BlockingQueue<Booking> bookingsQueue = new LinkedBlockingQueue<>();


    public BookingQueueService(
            @Autowired BookingProducerService bookingProducerService,
            @Value("${booking-queue-service.workers:2}") int numberOfWorkers) {
        this.bookingProducerService = bookingProducerService;

        Thread[] workers = new Thread[numberOfWorkers];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(new BookingRunnable());
            workers[i].start();
        }
    }

    public boolean addBooking(Booking booking) {
        return bookingsQueue.add(booking);
    }

    private class BookingRunnable implements Runnable {

        @Override
        public void run() {
            for (; ; ) {
                try {
                    Booking booking = bookingsQueue.take();  // blocks until element is available
                    // verify booking data
                    produceBookingMessage(booking);
                } catch (InterruptedException e) {
                    log.error("Error fetching data from the bookings queue: ", e);
                }
            }
        }

        private void produceBookingMessage(Booking booking) {
            ListenableFuture<SendResult<String, String>> future =
                    bookingProducerService.produceBookingMessage(booking.getData());

            try {
                future
                        .completable()
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                saveToRetryTable(booking);
                            }
                        });
            } catch (Exception e) {
                saveToRetryTable(booking);
            }
        }

        private void saveToRetryTable(Booking booking) {
            // todo: implement
        }
    }
}
