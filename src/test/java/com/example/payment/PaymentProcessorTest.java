package com.example.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessor Tests")
class PaymentProcessorTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentNotifier paymentNotifier;

    private PaymentProcessor paymentProcessor;

    @BeforeEach
    void setUp() {
        paymentProcessor = new PaymentProcessor(paymentGateway, paymentRepository, paymentNotifier);
    }

    // ========== Input validation ==========

    @ParameterizedTest(name = "processPayment should throw IllegalArgumentException for invalid amount {0}")
    @ValueSource(doubles = {0.0, -1.0, -100.5})
    @DisplayName("processPayment should throw IllegalArgumentException for non-positive amounts")
    void processPayment_shouldThrowException_whenAmountIsNonPositive(double invalidAmount) {
        assertThatThrownBy(() -> paymentProcessor.processPayment("user@example.com", invalidAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be > 0");

        verifyNoInteractions(paymentGateway, paymentRepository, paymentNotifier);
    }

    @ParameterizedTest(name = "processPayment should throw IllegalArgumentException for invalid email \"{0}\"")
    @ValueSource(strings = { "", " ", "   " })
    @DisplayName("processPayment should throw IllegalArgumentException for blank email")
    void processPayment_shouldThrowException_whenEmailIsBlank(String invalidEmail) {
        assertThatThrownBy(() -> paymentProcessor.processPayment(invalidEmail, 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userEmail must be provided");

        verifyNoInteractions(paymentGateway, paymentRepository, paymentNotifier);
    }

    @Test
    @DisplayName("processPayment should throw IllegalArgumentException when email is null")
    void processPayment_shouldThrowException_whenEmailIsNull() {
        assertThatThrownBy(() -> paymentProcessor.processPayment(null, 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userEmail must be provided");

        verifyNoInteractions(paymentGateway, paymentRepository, paymentNotifier);
    }

    // ========== Successful payment ==========

    @Test
    @DisplayName("processPayment should return true and notify user when payment succeeds")
    void processPayment_shouldReturnTrue_andNotifyUser_whenPaymentSucceeds() throws PaymentNotificationException {
        double amount = 150.0;
        String email = "user@example.com";

        when(paymentGateway.charge(amount)).thenReturn(PaymentResult.success());

        boolean result = paymentProcessor.processPayment(email, amount);

        assertThat(result).isTrue();

        // Verify repository save
        ArgumentCaptor<PaymentRecord> recordCaptor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentRepository).save(recordCaptor.capture());
        PaymentRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.amount()).isEqualTo(amount);
        assertThat(savedRecord.status()).isEqualTo(PaymentStatus.SUCCESS);

        // Verify notifier
        verify(paymentNotifier).sendPaymentConfirmation(email, amount);
    }

    // ========== Failed payment ==========

    @Test
    @DisplayName("processPayment should return false and not notify user when payment fails")
    void processPayment_shouldReturnFalse_andNotNotifyUser_whenPaymentFails() throws PaymentNotificationException {
        double amount = 200.0;
        String email = "user@example.com";

        when(paymentGateway.charge(amount)).thenReturn(PaymentResult.failed("Insufficient funds"));

        boolean result = paymentProcessor.processPayment(email, amount);

        assertThat(result).isFalse();

        // Verify repository save
        ArgumentCaptor<PaymentRecord> recordCaptor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentRepository).save(recordCaptor.capture());
        PaymentRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.amount()).isEqualTo(amount);
        assertThat(savedRecord.status()).isEqualTo(PaymentStatus.FAILED);

        // No notification on failure
        verifyNoInteractions(paymentNotifier);
    }

    // ========== Notification failure resilience ==========

    @Test
    @DisplayName("processPayment should still return true when notification throws exception")
    void processPayment_shouldReturnTrue_whenNotificationThrowsException() throws PaymentNotificationException {
        double amount = 300.0;
        String email = "user@example.com";

        when(paymentGateway.charge(amount)).thenReturn(PaymentResult.success());
        doThrow(new PaymentNotificationException("SMTP error"))
                .when(paymentNotifier).sendPaymentConfirmation(email, amount);

        boolean result = paymentProcessor.processPayment(email, amount);

        assertThat(result).isTrue();

        // Repository should still be called with SUCCESS
        ArgumentCaptor<PaymentRecord> recordCaptor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentRepository).save(recordCaptor.capture());
        PaymentRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.amount()).isEqualTo(amount);
        assertThat(savedRecord.status()).isEqualTo(PaymentStatus.SUCCESS);

        // Notifier was called, men kastade exception
        verify(paymentNotifier).sendPaymentConfirmation(email, amount);
    }
}
