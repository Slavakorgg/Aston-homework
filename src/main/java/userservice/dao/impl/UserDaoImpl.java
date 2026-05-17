package userservice.dao.impl;

import userservice.config.HibernateUtil;
import userservice.dao.UserDao;
import userservice.entity.User;
import userservice.exception.DataAccessException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);
    private final SessionFactory sessionFactory;

    public UserDaoImpl() {
        this(HibernateUtil.getSessionFactory());
    }

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User create(User user) {
        return executeInTransaction(session -> {
            session.persist(user);
            return user;
        }, "Не удалось создать пользователя");
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        } catch (RuntimeException ex) {
            throw mapException("Не удалось получить пользователя по идентификатору: " + id, ex);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User order by id", User.class).list();
        } catch (RuntimeException ex) {
            throw mapException("Не удалось получить список пользователей", ex);
        }
    }

    @Override
    public User update(User user) {
        return executeInTransaction(session -> (User) session.merge(user),
                "Не удалось обновить пользователя с id: " + user.getId());
    }

    @Override
    public void deleteById(Long id) {
        executeInTransaction(session -> {
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
            }
            return null;
        }, "Не удалось удалить пользователя с id: " + id);
    }

    private <T> T executeInTransaction(SessionAction<T> action, String errorMessage) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            T result = action.execute(session);
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            rollbackQuietly(transaction);
            throw mapException(errorMessage, ex);
        } finally {
            session.close();
        }
    }

    private void rollbackQuietly(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            try {
                transaction.rollback();
            } catch (RuntimeException ex) {
                logger.error("Ошибка отката транзакции: {}", ex.getMessage());
            }
        }
    }

    private DataAccessException mapException(String fallbackMessage, Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolation) {
                return new DataAccessException(
                        "Нарушение ограничений данных (например, дублирующийся email)",
                        constraintViolation
                );
            }
            if (current instanceof JDBCConnectionException connectionException) {
                return new DataAccessException("Не удалось подключиться к PostgreSQL.", connectionException);
            }
            current = current.getCause();
        }
        if (ex instanceof HibernateException) {
            return new DataAccessException(fallbackMessage, ex);
        }
        return new DataAccessException(fallbackMessage, ex);
    }

    @FunctionalInterface
    private interface SessionAction<T> {
        T execute(Session session);
    }
}
