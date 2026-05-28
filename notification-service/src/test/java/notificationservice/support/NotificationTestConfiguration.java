package notificationservice.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class NotificationTestConfiguration {

    @Bean
    @Primary
    public CapturingNotificationSender capturingNotificationSender() {
        return new CapturingNotificationSender();
    }
}
