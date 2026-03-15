package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static com.game.entity.Player.GET_TOTAL_PLAYERS;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "mysql");
        properties.put(Environment.HBM2DDL_AUTO, "update");

        this.sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();

    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            Query<Player> query = session.createNativeQuery(
                    "SELECT * FROM rpg.player", Player.class);
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Ошибка при получении списка игроков: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createNamedQuery(GET_TOTAL_PLAYERS, Long.class);
            Long count = query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            System.err.println("Ошибка при получении количества игроков: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player save(Player player) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.persist(player);
            transaction.commit();
            return player;
        } catch (Exception e) {
            transaction.rollback();
            System.out.println("Ошибка при сохранении игрока с номером = : " + player.getId() + ": " + e.getMessage());
            throw new RuntimeException();
        } finally {
            session.close();
        }
    }

    @Override
    public Player update(Player player) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.merge(player);
            transaction.commit();
            return player;
        } catch (Exception e) {
            transaction.rollback();
            System.out.println("Ошибка при обновлении данных игрока с номером: " + player.getId() + ": " + e.getMessage());
            throw new RuntimeException();
        } finally {
            session.close();
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            Player player=session.find(Player.class,id);
            return Optional.ofNullable(player);
        } catch (Exception e) {
            System.out.println("Ошибка при поиске игрока номер : " + id + ": " + e.getMessage());
            throw new RuntimeException();
        }
    }

    @Override
    public void delete(Player player) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.delete(player);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            System.out.println("Ошибка при удалении игрока с номером = : " + player.getId() + ": " + e.getMessage());
            throw new RuntimeException();
        } finally {
            session.close();
        }
    }

    @PreDestroy
    public void beforeStop() {
       sessionFactory.close();
    }
}