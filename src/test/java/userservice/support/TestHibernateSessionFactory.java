package userservice.support;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import userservice.entity.User;


public final class TestHibernateSessionFactory {

    private TestHibernateSessionFactory() {
    }

    public static SessionFactory create(String jdbcUrl, String username, String password) {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", jdbcUrl);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.addAnnotatedClass(User.class);
        return configuration.buildSessionFactory();
    }
}
