package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }



    @GetMapping("/methods")
    public ResponseEntity<List<String>> getPaymentMethods(){
        return ResponseEntity.ok(paymentService.getAvailablePaymentMethods());
    }
    @PostMapping("/process")
    public ResponseEntity<String> processPayment(
            @RequestParam String method,
            @RequestParam double amount
    ){
        try {
            boolean success = paymentService.processPayment(method,amount);
            if(success){
                return ResponseEntity.ok("Payment processed success");
            }
            return ResponseEntity.badRequest().body("Payment failed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
