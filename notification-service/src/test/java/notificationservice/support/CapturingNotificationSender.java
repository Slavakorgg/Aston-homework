package notificationservice.support;

import notificationservice.service.NotificationSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CapturingNotificationSender implements NotificationSender {

    private final List<SentNotification> sentNotifications = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void send(String email, String text) {
        sentNotifications.add(new SentNotification(email, text));
    }

    public List<SentNotification> getSentNotifications() {
        return List.copyOf(sentNotifications);
    }

    public void clear() {
        sentNotifications.clear();
    }

    public record SentNotification(String email, String text) {
    }
}
