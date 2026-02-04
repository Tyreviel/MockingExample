package com.example.payment;

/**
 * Abstraction over an external payment provider.
 *
 * <p>A production implementation might call Stripe/Adyen/etc. Tests can replace it with a stub.</p>
 */
public interface PaymentGateway {
    /**
     * Attempts to charge the given amount.
     *
     * @param amount amount to charge (assumed validated by caller)
     * @return result with status and optional failure details
     */
    PaymentResult charge(double amount);
}

