package userservice.service;

import userservice.dao.UserDao;
import userservice.entity.User;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUser(String name, String email, Integer age) {
        validate(name, email, age);
        return userDao.create(new User(name.trim(), email.trim(), age));
    }

    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public Optional<User> updateUser(Long id, String name, String email, Integer age) {
        validate(name, email, age);
        Optional<User> existing = userDao.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        User user = existing.get();
        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setAge(age);
        return Optional.of(userDao.update(user));
    }

    public boolean deleteUser(Long id) {
        Optional<User> existing = userDao.findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        userDao.deleteById(id);
        return true;
    }

    private void validate(String name, String email, Integer age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не должно быть пустым");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Некорректная электронная почта");
        }
        if (age == null || age < 0 || age > 130) {
            throw new IllegalArgumentException("Возраст должен быть в диапазоне от 0 до 130");
        }
    }
}
