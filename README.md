This project is an integration of two services actualized through the utilization of Kafka.

The CQRS architecture is used to organize the design of the two services. The `booking-service` 
is the Read API. The `payment-service` is the Write API.

Below is a diagram demonstrating the design for this project:

![BookingIntegration.jpg](BookingIntegration.jpg)

The `booking-service` responds to "create booking" calls by sending the request to a queue. Then, if the message is 
successfully added to the queue, a true message is sent to the client.

The queue is a shared memory data structure between threads that send--or produce--the event to the Kafka topic.

This functionality is implemented in the `BookingQueueService`, which is depicted in the diagram below:

![BookingQueueService.jpg](BookingQueueService.jpg)

The following is pending development:

- save failed booking calls to a retry table (and nightly cron job)
- MySQL database for users, payments, bookings, and listings (integrated with Write API)
- Redis reflection of the above data (integrated with Read API)
- an adapter between MySQL and Redis that updates Redis when changes are made to the MySQL database