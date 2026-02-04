package com.example.payment;

/**
 * Persistence abstraction for payment records.
 *
 * <p>A production implementation could write to a database. Unit tests can use a fake.</p>
 */
public interface PaymentRepository {
    void save(PaymentRecord record);
}

