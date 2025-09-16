package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate; // Inject RestTemplate bean

    @KafkaListener(topics = "${general.kafka-topic}")
    public void consume(Transaction transaction) {
        LOG.info("--> TRANSACTION RECEIVED - Amount: {}", transaction.getAmount());

        Optional<UserRecord> senderOptional = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOptional = userRepository.findById(transaction.getRecipientId());

        if (senderOptional.isPresent() && recipientOptional.isPresent()) {
            UserRecord sender = senderOptional.get();
            UserRecord recipient = recipientOptional.get();

            if (sender.getBalance() >= transaction.getAmount()) {
                // Deduct amount from sender
                sender.setBalance(sender.getBalance() - transaction.getAmount());

                // Call Incentive API
                Incentive incentive = null;
                try {
                    incentive = restTemplate.postForObject(
                            "http://localhost:8080/incentive",
                            transaction,
                            Incentive.class
                    );
                } catch (Exception e) {
                    LOG.warn("Incentive API call failed: {}", e.getMessage());
                }

                float incentiveAmount = (incentive != null) ? incentive.getAmount() : 0f;

                // Add amount + incentive to recipient
                recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

                // Save updated users
                userRepository.save(sender);
                userRepository.save(recipient);

                // Save transaction with incentive
                TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
                transactionRepository.save(transactionRecord);

                LOG.info("Transaction processed successfully. Incentive added: {}", incentiveAmount);
            } else {
                LOG.warn("Transaction discarded: Insufficient balance.");
            }
        } else {
            LOG.warn("Transaction discarded: Invalid sender or recipient ID.");
        }
    }
}
