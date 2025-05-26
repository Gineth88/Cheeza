
package com.cheeza.Cheeza.dto;

public class PaymentDetails {
    public String cardNumber;
    public String expiryDate;
    public String cvv;
    public String cardHolderName; // NEW: Needed for full card info

    public String walletId;
    public int pointsToRedeem;

    public PaymentDetails() {}
}
