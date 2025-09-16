package com.jpmc.midascore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;

@Entity
public class UserRecord {
    @Id
    private long id;

    private String userName;

    private float balance;

    // This tells JPA to look for the 'sender' field in the TransactionRecord class
    @OneToMany(mappedBy = "sender")
    private Set<TransactionRecord> sentTransactions;

    // This tells JPA to look for the 'recipient' field in the TransactionRecord class
    @OneToMany(mappedBy = "recipient")
    private Set<TransactionRecord> receivedTransactions;


    public UserRecord() {
    }

    public UserRecord(long id, String userName, float balance) {
        this.id = id;
        this.userName = userName;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }
}