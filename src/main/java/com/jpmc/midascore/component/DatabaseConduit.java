package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Correct Spring import

import java.util.Optional;

@Component
public class DatabaseConduit {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // Using constructor injection is a best practice
    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void processTransaction(Transaction transaction) {
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());

        // 1. Validate that both sender and recipient exist in the database.
        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // Discard transaction if either user is not found.
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();
        float amount = transaction.getAmount();

        // 2. Validate that the sender has a sufficient balance.
        if (sender.getBalance() < amount) {
            return; // Discard transaction due to insufficient funds.
        }

        // 3. Perform the balance transfer.
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // 4. Save the updated user balances to the database.
        userRepository.save(sender);
        userRepository.save(recipient);

        // 5. Create a new record of the transaction and save it.
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, amount);
        transactionRepository.save(transactionRecord);
    }
}

