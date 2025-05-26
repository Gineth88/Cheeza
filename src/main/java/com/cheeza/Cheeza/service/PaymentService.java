package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final Map<String, PaymentStrategy>paymentStrategies;

    @Autowired
    public PaymentService(List<PaymentStrategy> strategies){
        this.paymentStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getClass().getSimpleName(),
                        Function.identity()
                ));

    }

    public boolean processPayment(String strategyName , double amount){
        PaymentStrategy strategy = paymentStrategies.get(strategyName);
        if(strategy == null) {
            throw new IllegalArgumentException("Invalid payment strategy");
        }
        return strategy.processPayment(amount);
    }
    public List<String>getAvailablePaymentMethods(){
        return new ArrayList<>(paymentStrategies.keySet());
    }
}
