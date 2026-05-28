package notificationservice.service;

import notificationservice.dto.NotificationResponseDto;
import notificationservice.dto.UserOperation;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationSender notificationSender;

    public NotificationService(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public NotificationResponseDto send(UserOperation operation, String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }
        String text = resolveMessage(operation);
        notificationSender.send(email.trim(), text);
        return new NotificationResponseDto("Сообщение на email " + email.trim() + " было отправлено");
    }

    private String resolveMessage(UserOperation operation) {
        return switch (operation) {
            case CREATE -> "Здравствуйте! Ваш аккаунт был успешно создан.";
            case DELETE -> "Здравствуйте! Ваш аккаунт был удалён.";
        };
    }
}
