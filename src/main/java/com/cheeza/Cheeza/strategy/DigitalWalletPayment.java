package com.cheeza.Cheeza.strategy;

import org.springframework.stereotype.Component;

@Component
public class DigitalWalletPayment implements PaymentStrategy{
//    private String walletId;

//    public DigitalWalletPayment(String walletId) {
//        this.walletId = walletId;
//    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("Processing digital wallet payment of $" + amount);
//        System.out.println("Wallet ID: " + walletId);
        return true;
    }

    @Override
    public String getPaymentMethodName() {
        return "Digital Wallet";
    }
}
