package com.example.payment;

import java.util.Objects;



public final class PaymentProcessor {
    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final PaymentNotifier paymentNotifier;

    /**
     * @param paymentGateway gateway to charge the customer (external provider)
     * @param paymentRepository repository used to persist payment attempts/outcomes
     * @param paymentNotifier notifier used to send confirmations/receipts
     */
    public PaymentProcessor(
            PaymentGateway paymentGateway,
            PaymentRepository paymentRepository,
            PaymentNotifier paymentNotifier
    ) {
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "paymentGateway");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "paymentRepository");
        this.paymentNotifier = Objects.requireNonNull(paymentNotifier, "paymentNotifier");
    }

    /**
     * Attempts to charge a given amount.

     * Behavior:
     *
     *   Rejects non-positive amounts
     *   Charges via {@link PaymentGateway}
     *   Persists a record in {@link PaymentRepository}
     *   If successful: sends a confirmation via {@link PaymentNotifier}
     *
     * Notification failures should not flip a successful charge into a failed payment.
     *
     * @param userEmail email address to send confirmation to (required for success notifications)
     * @param amount amount to charge (must be > 0)
     * @return {@code true} if the charge succeeded; otherwise {@code false}
     */
    public boolean processPayment(String userEmail, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException("userEmail must be provided");
        }

        PaymentResult result = paymentGateway.charge(amount);
        paymentRepository.save(PaymentRecord.of(amount, result.status()));

        if (result.isSuccess()) {
            try {
                paymentNotifier.sendPaymentConfirmation(userEmail, amount);
            } catch (PaymentNotificationException e) {
                // Continue even if notification fails (business decision).
            }
        }

        return result.isSuccess();
    }
}
