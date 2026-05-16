package userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import userservice.dao.UserDao;
import userservice.entity.User;

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
    private UserDao userDao;

    @InjectMocks
    private UserService userService;


    @Test
    void createUser_shouldValidateAndDelegateToDao() {//проверка корректного создания пользователя
        when(userDao.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser("  Иван  ", "  ivan@test.com  ", 25);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).create(captor.capture());
        assertEquals("Иван", captor.getValue().getName());
        assertEquals("ivan@test.com", captor.getValue().getEmail());
        assertEquals(25, captor.getValue().getAge());
    }


    @Test
    void createUser_shouldThrowWhenNameIsBlank() {//проверка имени
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("  ", "ivan@test.com", 25));
        verify(userDao, never()).create(any());
    }


    @Test
    void createUser_shouldThrowWhenEmailIsInvalid() {//проверка email
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("Иван", "invalid", 25));
        verify(userDao, never()).create(any());
    }


    @Test
    void createUser_shouldThrowWhenAgeIsOutOfRange() {//проверка возраста
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("Иван", "ivan@test.com", 200));
        verify(userDao, never()).create(any());
    }


    @Test
    void getUserById_shouldReturnUserFromDao() {//проверка получения по id
        User user = new User("Иван", "ivan@test.com", 25);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("Иван", result.get().getName());
        verify(userDao).findById(1L);
    }


    @Test
    void getAllUsers_shouldReturnListFromDao() {//проверка получения всех пользователей
        User user = new User("Иван", "ivan@test.com", 25);
        when(userDao.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        verify(userDao).findAll();
    }


    @Test
    void updateUser_shouldUpdateExistingUser() {//проверка обновления пользователя
        User existing = new User("Иван", "ivan@test.com", 25);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.update(existing)).thenReturn(existing);

        Optional<User> result = userService.updateUser(1L, "Пётр", "petr@test.com", 30);

        assertTrue(result.isPresent());
        assertEquals("Пётр", existing.getName());
        assertEquals("petr@test.com", existing.getEmail());
        assertEquals(30, existing.getAge());
        verify(userDao).update(existing);
    }


    @Test
    void updateUser_shouldReturnEmptyWhenUserNotFound() {//проверка обновления несуществующего пользователя
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(99L, "Пётр", "petr@test.com", 30);

        assertTrue(result.isEmpty());
        verify(userDao, never()).update(any());
    }


    @Test
    void deleteUser_shouldReturnTrueWhenUserExists() {//проверка удаления
        User existing = new User("Иван", "ivan@test.com", 25);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));

        boolean deleted = userService.deleteUser(1L);

        assertTrue(deleted);
        verify(userDao).deleteById(1L);
    }


    @Test
    void deleteUser_shouldReturnFalseWhenUserNotFound() {//проверка удаления несуществующего пользователя
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        boolean deleted = userService.deleteUser(99L);

        assertFalse(deleted);
        verify(userDao, never()).deleteById(anyLong());
    }
}
