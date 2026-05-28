package userservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import userservice.entity.User;
import userservice.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_shouldReturnDtoList() throws Exception {
        User user = new User("Иван", "ivan@test.com", 25);
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Иван"))
                .andExpect(jsonPath("$[0].email").value("ivan@test.com"));
    }

    @Test
    void getUserById_shouldReturnDtoWhenFound() throws Exception {
        User user = new User("Иван", "ivan@test.com", 25);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иван"));
    }

    @Test
    void getUserById_shouldReturn404WhenNotFound() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_shouldReturn201AndDto() throws Exception {
        User created = new User("Иван", "ivan@test.com", 25);
        when(userService.createUser(eq("Иван"), eq("ivan@test.com"), eq(25))).thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иван","email":"ivan@test.com","age":25}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan@test.com"));
    }

    @Test
    void updateUser_shouldReturnDtoWhenFound() throws Exception {
        User updated = new User("Пётр", "petr@test.com", 30);
        when(userService.updateUser(eq(1L), eq("Пётр"), eq("petr@test.com"), eq(30)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Пётр","email":"petr@test.com","age":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Пётр"));
    }

    @Test
    void updateUser_shouldReturn404WhenNotFound() throws Exception {
        when(userService.updateUser(eq(99L), any(), any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Пётр","email":"petr@test.com","age":30}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturn204WhenDeleted() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturn404WhenNotFound() throws Exception {
        when(userService.deleteUser(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
