package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.jpmc.midascore.foundation.Transaction;

@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue()
    private long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserRecord sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserRecord recipient;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private boolean valid;

    @Column(nullable = false)
    private float incentive;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected TransactionRecord() {
    }

    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, boolean valid, float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.valid = valid;
        this.incentive = incentive;
        this.timestamp = LocalDateTime.now();
    }

    public static TransactionRecord fromTransaction(Transaction transaction, UserRecord sender, UserRecord recipient, boolean valid, float incentive) {
        return new TransactionRecord(sender, recipient, transaction.getAmount(), valid, incentive);
    }

    public long getId() {
        return id;
    }

    public UserRecord getSender() {
        return sender;
    }

    public UserRecord getRecipient() {
        return recipient;
    }

    public float getAmount() {
        return amount;
    }

    public boolean isValid() {
        return valid;
    }

    public float getIncentive() {
        return incentive;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("TransactionRecord[id=%d, sender=%s, recipient=%s, amount=%f, valid=%b, incentive=%f]", id, sender, recipient, amount, valid, incentive);
    }
}
