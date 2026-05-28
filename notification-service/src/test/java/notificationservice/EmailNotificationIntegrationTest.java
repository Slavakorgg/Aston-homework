package notificationservice;

import notificationservice.dto.UserEventMessage;
import notificationservice.support.CapturingNotificationSender;
import notificationservice.support.NotificationTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"user-events"})
@Import(NotificationTestConfiguration.class)
@ActiveProfiles("test")
class EmailNotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaTemplate<String, UserEventMessage> kafkaTemplate;

    @Autowired
    private CapturingNotificationSender capturingNotificationSender;

    @Value("${app.kafka.topic}")
    private String topic;

    @BeforeEach
    void setUp() {
        capturingNotificationSender.clear();
    }

    @Test
    void api_create_shouldSendEmailWithCreatedText() throws Exception { //отправка при создании через api
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operation":"CREATE","email":"ivan@test.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Сообщение на email ivan@test.com было отправлено"));

        var sent = capturingNotificationSender.getSentNotifications();
        assertEquals(1, sent.size());
        assertEquals("ivan@test.com", sent.get(0).email());
        assertEquals("Здравствуйте! Ваш аккаунт был успешно создан.", sent.get(0).text());
    }

    @Test
    void api_delete_shouldSendEmailWithDeletedText() throws Exception { //отправка при удалении через api
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operation":"DELETE","email":"petr@test.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Сообщение на email petr@test.com было отправлено"));

        var sent = capturingNotificationSender.getSentNotifications();
        assertEquals(1, sent.size());
        assertEquals("petr@test.com", sent.get(0).email());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", sent.get(0).text());
    }

    @Test
    void api_invalidEmail_shouldNotSendEmail() throws Exception { //невалидный email
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operation":"CREATE","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest());

        assertTrue(capturingNotificationSender.getSentNotifications().isEmpty());
    }

    @Test
    void kafka_create_shouldSendEmailWithCreatedText() { //отправка при создании кафка
        kafkaTemplate.send(topic, new UserEventMessage("CREATE", "kafka-create@test.com"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var sent = capturingNotificationSender.getSentNotifications();
            assertEquals(1, sent.size());
            assertEquals("kafka-create@test.com", sent.get(0).email());
            assertEquals("Здравствуйте! Ваш аккаунт был успешно создан.", sent.get(0).text());
        });
    }

    @Test
    void kafka_delete_shouldSendEmailWithDeletedText() { //отправка при удалении кафка
        kafkaTemplate.send(topic, new UserEventMessage("DELETE", "kafka-delete@test.com"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var sent = capturingNotificationSender.getSentNotifications();
            assertEquals(1, sent.size());
            assertEquals("kafka-delete@test.com", sent.get(0).email());
            assertEquals("Здравствуйте! Ваш аккаунт был удалён.", sent.get(0).text());
        });
    }
}
