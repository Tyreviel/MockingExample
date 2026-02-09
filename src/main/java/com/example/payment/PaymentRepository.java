package com.example.payment;

/**
 * Persistence abstraction for payment records.
 *
 */
public interface PaymentRepository {
    void save(PaymentRecord record);
}

