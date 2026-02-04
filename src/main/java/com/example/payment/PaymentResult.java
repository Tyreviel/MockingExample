package com.example.payment;

import java.util.Objects;
import java.util.Optional;

/**
 * Result from attempting to charge a payment.
 */
public final class PaymentResult {
    private final PaymentStatus status;
    private final String failureReason;

    private PaymentResult(PaymentStatus status, String failureReason) {
        this.status = Objects.requireNonNull(status, "status");
        this.failureReason = failureReason;
    }

    public static PaymentResult success() {
        return new PaymentResult(PaymentStatus.SUCCESS, null);
    }

    public static PaymentResult failed(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must be provided for failed payments");
        }
        return new PaymentResult(PaymentStatus.FAILED, reason);
    }

    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }

    public PaymentStatus status() {
        return status;
    }

    public Optional<String> failureReason() {
        return Optional.ofNullable(failureReason);
    }
}

