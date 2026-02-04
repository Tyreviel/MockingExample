package com.example.payment;

/**
 * Signals that sending a payment confirmation failed.
 */
public class PaymentNotificationException extends Exception {
    public PaymentNotificationException(String message) {
        super(message);
    }

    public PaymentNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

