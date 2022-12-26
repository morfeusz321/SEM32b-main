package nl.tudelft.sem.template.request;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.sem.template.request.event.RequestCreatedEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * Example microservice application.
 */
@Slf4j
@SpringBootApplication
public class RequestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestApplication.class, args);
    }

    @KafkaListener(topics = "notificationTopic")
    public void handleNotification(RequestCreatedEvent requestCreatedEvent) {
        //send out email notification
        log.info("Received Notification for Request - {}", requestCreatedEvent.getRequestId());
    }
}
