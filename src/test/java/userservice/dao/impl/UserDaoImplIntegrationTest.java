package userservice.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import userservice.entity.User;
import userservice.exception.DataAccessException;
import userservice.support.AbstractPostgresIntegrationTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class UserDaoImplIntegrationTest extends AbstractPostgresIntegrationTest {

    private UserDaoImpl userDao;

    @BeforeEach
    void setUpDao() {
        userDao = new UserDaoImpl(testSessionFactory);
    }


    @Test
    void create_shouldPersistUserWithGeneratedId() { //проверка сохранения ползователя и присваивания id
        User user = new User("Иван", "ivan@test.com", 25);

        User created = userDao.create(user);

        assertNotNull(created.getId());
        assertEquals("Иван", created.getName());
        assertNotNull(created.getCreatedAt());
    }


    @Test
    void findById_shouldReturnPersistedUser() { //чтение пользователя по id
        User created = userDao.create(new User("Иван", "ivan@test.com", 25));

        Optional<User> found = userDao.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("ivan@test.com", found.get().getEmail());
    }


    @Test
    void findById_shouldReturnEmptyForUnknownId() { //возвращение несуществующего пользователя
        Optional<User> found = userDao.findById(999_999L);

        assertTrue(found.isEmpty());
    }


    @Test
    void findAll_shouldReturnAllUsersOrderedById() { //возвращение всех пользователей
        userDao.create(new User("Первый", "first@test.com", 20));
        userDao.create(new User("Второй", "second@test.com", 30));

        List<User> users = userDao.findAll();

        assertEquals(2, users.size());
        assertTrue(users.get(0).getId() < users.get(1).getId());
    }


    @Test
    void update_shouldChangeUserFields() { //обновление пользователя
        User created = userDao.create(new User("Иван", "ivan@test.com", 25));
        created.setName("Пётр");
        created.setEmail("petr@test.com");
        created.setAge(30);

        User updated = userDao.update(created);

        Optional<User> found = userDao.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Пётр", found.get().getName());
        assertEquals("petr@test.com", found.get().getEmail());
        assertEquals(30, found.get().getAge());
    }


    @Test
    void deleteById_shouldRemoveUser() { //удаление пользователя
        User created = userDao.create(new User("Иван", "ivan@test.com", 25));

        userDao.deleteById(created.getId());

        Optional<User> found = userDao.findById(created.getId());
        assertTrue(found.isEmpty());
    }


    @Test
    void create_duplicateEmail_shouldThrowDataAccessException() {//проверка уникального email
        userDao.create(new User("Иван", "dup@test.com", 25));

        assertThrows(DataAccessException.class,
                () -> userDao.create(new User("Пётр", "dup@test.com", 30)));
    }
}
