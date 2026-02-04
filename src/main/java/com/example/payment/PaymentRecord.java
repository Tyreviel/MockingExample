package com.example.payment;

import java.util.Objects;

/**
 * Minimal persisted record of a payment attempt.
 *
 * <p>Kept intentionally small for the school example; you can extend this later with IDs, timestamps,
 * provider references, etc.</p>
 */
public final class PaymentRecord {
    private final double amount;
    private final PaymentStatus status;

    private PaymentRecord(double amount, PaymentStatus status) {
        this.amount = amount;
        this.status = Objects.requireNonNull(status, "status");
    }

    public static PaymentRecord of(double amount, PaymentStatus status) {
        return new PaymentRecord(amount, status);
    }

    public double amount() {
        return amount;
    }

    public PaymentStatus status() {
        return status;
    }
}

