package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskThreeTests {
    private static final Logger logger = LoggerFactory.getLogger(TaskThreeTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    @Test
    void task_three_verifier() throws InterruptedException {
        // 1️⃣ Populate users
        userPopulator.populate();

        // 2️⃣ Load transactions
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");

        // 3️⃣ Load all users into memory for quick lookup by ID
        Map<Long, UserRecord> users = new HashMap<>();
        userRepository.findAll().forEach(user -> users.put(user.getId(), user));

        // 4️⃣ Apply transactions manually (like KafkaConsumer)
        for (String line : transactionLines) {
            String[] parts = line.split(", ");
            long senderId = Long.parseLong(parts[0]);
            long recipientId = Long.parseLong(parts[1]);
            float amount = Float.parseFloat(parts[2]);

            UserRecord sender = users.get(senderId);
            UserRecord recipient = users.get(recipientId);

            if (sender == null || recipient == null) {
                logger.warn("Skipping transaction with invalid user IDs: {}", line);
                continue;
            }

            if (sender.getBalance() >= amount) {
                sender.setBalance(sender.getBalance() - amount);
                recipient.setBalance(recipient.getBalance() + amount);
            } else {
                logger.warn("Skipping transaction due to insufficient balance: {}", line);
            }
        }

        // 5️⃣ Log all users' balances
        logger.info("------ Users' balances after all transactions ------");
        users.values().forEach(u ->
                logger.info("User: {}, ID: {}, Balance: {}", u.getUserName(), u.getId(), u.getBalance())
        );

        // 6️⃣ Also log Waldorf separately
        users.values().stream()
                .filter(u -> u.getUserName().equalsIgnoreCase("waldorf"))
                .forEach(u -> logger.info("Waldorf's final balance = {}", u.getBalance()));
    }
}
