package com.cheeza.Cheeza.strategy;

public interface PaymentStrategy {
    boolean processPayment(double amount);
    String getPaymentMethodName();
}
