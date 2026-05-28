package notificationservice.service;

import notificationservice.dto.NotificationResponseDto;
import notificationservice.dto.UserOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationSender notificationSender;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationSender);
    }

    @Test
    void send_create_shouldSendCreatedEmailText() { //отправка при создании
        NotificationResponseDto response = notificationService.send(UserOperation.CREATE, "ivan@test.com");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationSender).send(emailCaptor.capture(), textCaptor.capture());

        assertEquals("ivan@test.com", emailCaptor.getValue());
        assertEquals("Здравствуйте! Ваш аккаунт был успешно создан.", textCaptor.getValue());
        assertEquals("Сообщение на email ivan@test.com было отправлено", response.getMessage());
    }

    @Test
    void send_delete_shouldSendDeletedEmailText() { //отправка при удалении
        notificationService.send(UserOperation.DELETE, "petr@test.com");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationSender).send(emailCaptor.capture(), textCaptor.capture());

        assertEquals("petr@test.com", emailCaptor.getValue());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", textCaptor.getValue());
    }

    @Test
    void send_invalidEmail_shouldThrowAndNotSend() { //невалидный email
        assertThrows(IllegalArgumentException.class,
                () -> notificationService.send(UserOperation.CREATE, "invalid-email"));

        verifyNoInteractions(notificationSender);
    }
}
