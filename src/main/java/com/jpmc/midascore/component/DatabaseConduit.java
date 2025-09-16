package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public class DatabaseConduit {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<UserRecord> findUserById(String userId) {
        return Optional.ofNullable(entityManager.find(UserRecord.class, userId));
    }

    public void saveUser(UserRecord user) {
        entityManager.merge(user);
    }

    public void saveTransaction(TransactionRecord transaction) {
        entityManager.persist(transaction);
    }
}
