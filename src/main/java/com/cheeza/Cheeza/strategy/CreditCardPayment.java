
package com.cheeza.Cheeza.strategy;

import com.cheeza.Cheeza.dto.PaymentDetails;
import com.cheeza.Cheeza.model.CreditCardInfo;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.CreditCardInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreditCardPayment implements PaymentStrategy {

    @Autowired
    private CreditCardInfoRepository cardRepo;



    @Override
    public String getPaymentMethodName() { return "CreditCard"; }

//    @Override
//    public boolean processPayment(PaymentDetails details, double amount, User user) {
//        if (details.cardNumber == null || details.expiryDate == null || details.cvv == null || details.cardHolderName == null)
//            return false;
//
//        // (Mock) Validate details...
//        System.out.println("[MOCK] Credit card payment: " + amount);
//
//        // Mask card number (e.g. "************1234")
//        String masked = details.cardNumber.replaceAll("\\d(?=\\d{4})", "*");
//
//        CreditCardInfo info = new CreditCardInfo();
//        info.setUser(user);
//        info.setCardNumberMasked(masked);
//        info.setCardHolderName(details.cardHolderName);
//        info.setExpiry(details.expiryDate);
//
//        cardRepo.save(info);
//        // Never store or log CVV!
//        return true;
//    }

    @Override
    public boolean processPayment(double amount) {
        // In real life, you'd receive card info via parameters or DTO (for mock, just print).
        System.out.println("Processing credit card payment of LKR:" + amount);
        return true; // Always true in mock
    }
}
