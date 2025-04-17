package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.service.OrderService;
import com.cheeza.Cheeza.service.PizzaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MenuController {
    private final PizzaService pizzaService;
    private final OrderService orderService;

    public MenuController(PizzaService pizzaService, OrderService orderService) {
        this.pizzaService = pizzaService;
        this.orderService = orderService;
    }

    @GetMapping("/menu")
    public String showMenu(Model model){
        model.addAttribute("pizzas",pizzaService.getAllPizzas());
        return "menu";
    }
    @PostMapping("/order/submit")
    public String submitOrder(@ModelAttribute Order order) {
        orderService.save(order);
        return "redirect:/order/success";
    }

    @PostMapping("/cart/add/{pizzaId}")
    public String addToCart(@PathVariable Long pizzaId, @RequestParam(defaultValue = "1") int quantity) {
        Pizza pizza = pizzaService.getPizzaById(pizzaId).orElseThrow();
        cartService.addPizza(pizza, quantity);
        return "redirect:/menu";
    }
}
