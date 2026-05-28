package userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import userservice.dto.UserEvent;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;

    public KafkaProducerService(
            KafkaTemplate<String, UserEvent> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendEvent(UserEvent event) {
        kafkaTemplate.send(topic, event.getEmail(), event);
        logger.info("Событие отправлено в Kafka: operation={}, email={}", event.getOperation(), event.getEmail());
    }
}
