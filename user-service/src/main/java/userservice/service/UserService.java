package userservice.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userservice.dto.UserEvent;
import userservice.dto.UserOperation;
import userservice.entity.User;
import userservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public UserService(UserRepository userRepository, KafkaProducerService kafkaProducerService) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public User createUser(String name, String email, Integer age) {
        validate(name, email, age);
        String normalizedEmail = email.trim();
        User created = userRepository.save(new User(name.trim(), normalizedEmail, age));
        kafkaProducerService.sendEvent(new UserEvent(UserOperation.CREATE.name(), normalizedEmail));
        return created;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by("id"));
    }

    @Transactional
    public Optional<User> updateUser(Long id, String name, String email, Integer age) {
        validate(name, email, age);
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(name.trim());
                    user.setEmail(email.trim());
                    user.setAge(age);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public boolean deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return false;
        }
        String email = userOptional.get().getEmail();
        userRepository.deleteById(id);
        kafkaProducerService.sendEvent(new UserEvent(UserOperation.DELETE.name(), email));
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
