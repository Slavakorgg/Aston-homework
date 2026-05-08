package userservice.ui;

import userservice.entity.User;
import userservice.exception.DataAccessException;
import userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleMenu {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleMenu.class);
    private final UserService userService;
    private final Scanner scanner;

    public ConsoleMenu(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String command = scanner.nextLine().trim();
            try {
                switch (command) {
                    case "1" -> createUser();
                    case "2" -> getUserById();
                    case "3" -> getAllUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "0" -> {
                        System.out.println("Выход из приложения.");
                        running = false;
                    }
                    default -> System.out.println("Неизвестная команда. Повторите ввод.");
                }
            } catch (IllegalArgumentException ex) {
                logger.warn("Ошибка валидации: {}", ex.getMessage());
                System.out.println("Ошибка валидации: " + ex.getMessage());
            } catch (DataAccessException ex) {
                logger.error("Ошибка операции с базой данных: {}", ex.getMessage());
                System.out.println("Ошибка операции с базой данных: " + ex.getMessage());
            } catch (Exception ex) {
                logger.error("Непредвиденная ошибка: {}", ex.getMessage());
                System.out.println("Непредвиденная ошибка: " + ex.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=== Меню ===");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Получить пользователя по идентификатору");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выход");
        System.out.print("Выберите пункт меню: ");
    }

    private void createUser() {
        String name = readString("Введите имя: ");
        String email = readString("Введите email: ");
        Integer age = readInt("Введите возраст: ");
        User created = userService.createUser(name, email, age);
        System.out.println("Пользователь создан: " + created);
    }

    private void getUserById() {
        Long id = readLong("Введите id: ");
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            System.out.println(user.get());
        } else {
            System.out.println("Пользователь не найден.");
        }
    }

    private void getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены.");
            return;
        }
        users.forEach(System.out::println);
    }

    private void updateUser() {
        Long id = readLong("Введите id: ");
        String name = readString("Введите новое имя: ");
        String email = readString("Введите новый email: ");
        Integer age = readInt("Введите новый возраст: ");

        Optional<User> updated = userService.updateUser(id, name, email, age);
        if (updated.isPresent()) {
            System.out.println("Пользователь обновлен: " + updated.get());
        } else {
            System.out.println("Пользователь не найден.");
        }
    }

    private void deleteUser() {
        Long id = readLong("Введите id: ");
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            System.out.println("Пользователь удален.");
        } else {
            System.out.println("Пользователь не найден.");
        }
    }

    private String readString(String stringInfo) {
        System.out.print(stringInfo);
        return scanner.nextLine();
    }

    private Integer readInt(String integerInfo) {
        System.out.print(integerInfo);
        String value = scanner.nextLine().trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Возраст должен быть целым числом", ex);
        }
    }

    private Long readLong(String longInfo) {
        System.out.print(longInfo);
        String value = scanner.nextLine().trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("id должен быть целым числом", ex);
        }
    }
}
