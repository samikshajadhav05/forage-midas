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

import java.util.List;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskFourTests {
    static final Logger logger = LoggerFactory.getLogger(TaskFourTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    @Test
    void task_four_verifier() throws InterruptedException {
        userPopulator.populate();
        String[] transactionLines = fileLoader.loadStrings("/test_data/alskdjfh.fhdjsk");

        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // Give Kafka time to process
        Thread.sleep(3000);

        // Print all balances
        logger.info("------ Users' balances after all transactions ------");
        List<UserRecord> users = userRepository.findAll();
        for (UserRecord user : users) {
            logger.info("User: {}, ID: {}, Balance: {}", user.getUserName(), user.getId(), user.getBalance());
        }

        logger.info("----------------------------------------------------");

        // Find Wilbur by looping through all users
        for (UserRecord user : users) {
            if ("wilbur".equalsIgnoreCase(user.getUserName())) {
                logger.info("Wilbur's final balance (rounded down): {}", (long) Math.floor(user.getBalance()));
            }
        }
    }
}
