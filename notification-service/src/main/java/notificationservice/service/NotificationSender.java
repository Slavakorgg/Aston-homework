package notificationservice.service;

public interface NotificationSender {

    void send(String email, String text);
}
