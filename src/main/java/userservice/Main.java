package userservice;

import userservice.config.HibernateUtil;
import userservice.dao.UserDao;
import userservice.dao.impl.UserDaoImpl;
import userservice.service.UserService;
import userservice.ui.ConsoleMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void Main(String[] args) {
        logger.warn("Запуск приложения");
        try {
            UserDao userDao = new UserDaoImpl();
            UserService userService = new UserService(userDao);
            ConsoleMenu menu = new ConsoleMenu(userService);
            menu.start();
        } catch (Exception ex) {
            logger.error("Ошибка запуска приложения: {}", ex.getMessage());
            System.err.println("Приложение завершилось с ошибкой: " + ex.getMessage());
        } finally {
            HibernateUtil.shutdown();
            logger.warn("Приложение остановлено");
        }
    }
}
