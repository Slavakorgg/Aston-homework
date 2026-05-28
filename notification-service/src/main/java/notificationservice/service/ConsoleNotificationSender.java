package notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ConsoleNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotificationSender.class);

    @Override
    public void send(String email, String text) {
        String deliveryMessage = "Сообщение на email " + email + " было отправлено";
        System.out.println(deliveryMessage);
        System.out.println(text);
        logger.info("{} | {}", deliveryMessage, text);
    }
}
