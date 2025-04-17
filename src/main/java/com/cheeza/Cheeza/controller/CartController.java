package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.service.CartService;
import com.cheeza.Cheeza.service.PizzaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    private  final PizzaService pizzaService;

    public CartController(CartService cartService, PizzaService pizzaService) {
        this.cartService = cartService;
        this.pizzaService = pizzaService;
    }

    @PostMapping("/add/{pizzaId}")
    public String addToCart(@PathVariable Long pizzaId,
                            @RequestParam(defaultValue = "1") int quantity){
        cartService.addToCart(pizzaId,quantity);
        return "redirect:/menu";

    }
    @GetMapping
    public String viewCart(Model model){
        model.addAttribute("cartItems", cartService.getOrderItem());
        model.addAttribute("total", cartService.getOrderItems().stream()
                .mapToDouble(item -> item.getPizza().getBasePrice() * item.getQuantity())
                .sum());
        return "cart";

    }
}
