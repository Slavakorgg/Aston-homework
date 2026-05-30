package notificationservice.kafka;

import notificationservice.dto.UserEventMessage;
import notificationservice.dto.UserOperation;
import notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);

    private final NotificationService notificationService;

    public UserEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(@Payload UserEventMessage event) {
        UserOperation operation = UserOperation.valueOf(event.getOperation());
        notificationService.send(operation, event.getEmail());
        logger.info("Обработано кафка событие: operation={}, email={}", operation, event.getEmail());
    }
}
