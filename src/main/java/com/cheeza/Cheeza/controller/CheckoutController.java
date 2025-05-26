
package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final PaymentService paymentService;

    @Autowired
    public CheckoutController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Show checkout page - safer handling for empty or missing cart total
    @GetMapping
    public String showCheckoutPage(Model model, HttpSession session) {
        Double totalObj = (Double) session.getAttribute("cartTotal");
        double total = totalObj != null ? totalObj : 0.0;

        List<String> paymentMethods = paymentService.getAvailablePaymentMethods();
        model.addAttribute("total", total);
        model.addAttribute("paymentMethods", paymentMethods);

        // Optionally handle empty cart scenario
        if (total == 0.0) {
            model.addAttribute("error", "Your cart is empty. Please add items before checking out.");
            // Optionally redirect back to cart page:
            // return "redirect:/cart";
        }

        return "checkout";
    }

    // Process checkout/payment
    @PostMapping
    public String processCheckout(@RequestParam String paymentMethod,
                                  HttpSession session,
                                  Model model) {
        Double totalObj = (Double) session.getAttribute("cartTotal");
        double total = totalObj != null ? totalObj : 0.0;

        if (total == 0.0) {
            model.addAttribute("error", "Cannot process payment for an empty cart.");
            return "checkout";
        }

        boolean paid = paymentService.processPayment(paymentMethod, total);
        if (paid) {
            // Clear cart, save order, etc.
            session.removeAttribute("cartItems");
            session.setAttribute("cartTotal", 0.0);
            model.addAttribute("success", "Payment successful and order placed!");
            return "redirect:/confirmation";
        } else {
            model.addAttribute("error", "Payment failed. Please try again.");
            return "checkout";
        }
    }
}
