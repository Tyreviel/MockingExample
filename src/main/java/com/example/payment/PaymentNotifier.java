package com.example.payment;

/**
 * Abstraction over notification/email delivery.
 */
public interface PaymentNotifier {
    void sendPaymentConfirmation(String userEmail, double amount) throws PaymentNotificationException;
}

