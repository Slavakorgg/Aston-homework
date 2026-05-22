package userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import userservice.entity.User;
import userservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldValidateAndDelegateToRepository() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser("  Иван  ", "  ivan@test.com  ", 25);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Иван", captor.getValue().getName());
        assertEquals("ivan@test.com", captor.getValue().getEmail());
        assertEquals(25, captor.getValue().getAge());
    }

    @Test
    void createUser_shouldThrowWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("  ", "ivan@test.com", 25));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldThrowWhenEmailIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("Иван", "invalid", 25));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldThrowWhenAgeIsOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("Иван", "ivan@test.com", 200));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnUserFromRepository() {
        User user = new User("Иван", "ivan@test.com", 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("Иван", result.get().getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getAllUsers_shouldReturnListFromRepository() {
        User user = new User("Иван", "ivan@test.com", 25);
        when(userRepository.findAll((Sort) any())).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).findAll((Sort) any());
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        User existing = new User("Иван", "ivan@test.com", 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        Optional<User> result = userService.updateUser(1L, "Пётр", "petr@test.com", 30);

        assertTrue(result.isPresent());
        assertEquals("Пётр", existing.getName());
        assertEquals("petr@test.com", existing.getEmail());
        assertEquals(30, existing.getAge());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_shouldReturnEmptyWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(99L, "Пётр", "petr@test.com", 30);

        assertTrue(result.isEmpty());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldReturnTrueWhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean deleted = userService.deleteUser(1L);

        assertTrue(deleted);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldReturnFalseWhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        boolean deleted = userService.deleteUser(99L);

        assertFalse(deleted);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
