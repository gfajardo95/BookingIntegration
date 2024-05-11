package com.fajardo.service;

import com.fajardo.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class BookingQueueService {

    private final BookingProducerService bookingProducerService;
    private final BlockingQueue<Booking> bookingsQueue = new LinkedBlockingQueue();

    private final Thread bookingWorker1;
    private final Thread bookingWorker2;

    public BookingQueueService(@Autowired BookingProducerService bookingProducerService) {
        this.bookingProducerService = bookingProducerService;

        bookingWorker1 = new Thread(new BookingRunnable());
        bookingWorker2 = new Thread(new BookingRunnable());

        bookingWorker1.start();
        bookingWorker2.start();
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
