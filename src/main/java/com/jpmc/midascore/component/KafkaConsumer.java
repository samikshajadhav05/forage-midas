package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @KafkaListener(topics = "${general.kafka-topic}")
    public void consume(Transaction transaction) {
        LOG.info("--> TRANSACTION RECEIVED - Amount: {}", transaction.getAmount());

        Optional<UserRecord> senderOptional = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOptional = userRepository.findById(transaction.getRecipientId());

        if (senderOptional.isPresent() && recipientOptional.isPresent()) {
            UserRecord sender = senderOptional.get();
            UserRecord recipient = recipientOptional.get();

            if (sender.getBalance() >= transaction.getAmount()) {
                // Update balances
                sender.setBalance(sender.getBalance() - transaction.getAmount());
                recipient.setBalance(recipient.getBalance() + transaction.getAmount());

                // Save updated user records
                userRepository.save(sender);
                userRepository.save(recipient);

                // Create and save transaction record
                TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount());
                transactionRepository.save(transactionRecord);

                LOG.info("Transaction processed successfully.");
            } else {
                LOG.warn("Transaction discarded: Insufficient balance.");
            }
        } else {
            LOG.warn("Transaction discarded: Invalid sender or recipient ID.");
        }
    }
}