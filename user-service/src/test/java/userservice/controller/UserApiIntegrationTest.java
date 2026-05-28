package userservice.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import userservice.service.KafkaProducerService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import userservice.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserApiIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_service_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void createAndGetUser_shouldWorkEndToEnd() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иван","email":"ivan@test.com","age":25}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        long id = JsonPath.parse(createResult.getResponse().getContentAsString())
                .read("$.id", Long.class);

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@test.com"));
    }

    @Test
    void updateUser_shouldChangeFields() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иван","email":"ivan@test.com","age":25}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long id = JsonPath.parse(created.getResponse().getContentAsString())
                .read("$.id", Long.class);

        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Пётр","email":"petr@test.com","age":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Пётр"))
                .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void deleteUser_shouldReturn404AfterDeletion() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иван","email":"ivan@test.com","age":25}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long id = JsonPath.parse(created.getResponse().getContentAsString())
                .read("$.id", Long.class);

        mockMvc.perform(delete("/api/users/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_duplicateEmail_shouldReturn409() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иван","email":"dup@test.com","age":25}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Пётр","email":"dup@test.com","age":30}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
}
