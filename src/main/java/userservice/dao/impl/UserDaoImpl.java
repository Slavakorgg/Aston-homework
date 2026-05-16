package userservice.dao.impl;

import userservice.config.HibernateUtil;
import userservice.dao.UserDao;
import userservice.entity.User;
import userservice.exception.DataAccessException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User create(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            return user;
        } catch (HibernateException ex) {
            rollbackQuietly(transaction);
            throw mapException("Не удалось создать пользователя", ex);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        } catch (HibernateException ex) {
            throw mapException("Не удалось получить пользователя по идентификатору: " + id, ex);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User order by id", User.class).list();
        } catch (HibernateException ex) {
            throw mapException("Не удалось получить список пользователей", ex);
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User merged = (User) session.merge(user);
            transaction.commit();
            return merged;
        } catch (HibernateException ex) {
            rollbackQuietly(transaction);
            throw mapException("Не удалось обновить пользователя с id: " + user.getId(), ex);
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
            }
            transaction.commit();
        } catch (HibernateException ex) {
            rollbackQuietly(transaction);
            throw mapException("Не удалось удалить пользователя с id: " + id, ex);
        }
    }

    private void rollbackQuietly(Transaction transaction) {
        if (transaction != null) {
            try {
                transaction.rollback();
            } catch (HibernateException ex) {
                logger.error("Ошибка HibernateException: {}", ex.getMessage());
            }
        }
    }

    private DataAccessException mapException(String fallbackMessage, HibernateException ex) {
        if (ex instanceof ConstraintViolationException) {
            return new DataAccessException("Нарушение ограничений данных (например, дублирующийся email)", ex);
        }
        if (ex instanceof JDBCConnectionException) {
            return new DataAccessException("Не удалось подключиться к PostgreSQL.", ex);
        }
        return new DataAccessException(fallbackMessage, ex);
    }
}
