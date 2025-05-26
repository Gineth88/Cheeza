package com.cheeza.Cheeza.strategy;

import org.springframework.stereotype.Component;

@Component
public class LoyaltyPointsPayment implements PaymentStrategy {
//    private int points;

//    public LoyaltyPointsPayment(int points) {
//        this.points = points;
//    }

    @Override
    public boolean processPayment(double amount) {
        // Mock conversion rate: 100 points = $1
//        double pointsValue = points / 100.0;
//        if (pointsValue >= amount) {
//            System.out.println("Paying with " + points + " loyalty points ($" + pointsValue + ")");
//            return true;
//        } else {
//            System.out.println("Insufficient points. Need " + (amount - pointsValue) +
//            return false;

//        }
        System.out.println("Mock: Loyalty points payment attempted for " + amount);
        return true; // Always return true for demonstration purposes
    }

    @Override
    public String getPaymentMethodName() {
        return "Loyalty Points";
    }
}