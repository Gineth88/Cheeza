package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.service.CartService;
import com.cheeza.Cheeza.service.OrderService;
import com.cheeza.Cheeza.service.PizzaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class MenuController {
    private final PizzaService pizzaService;
    private final OrderService orderService;
    private final CartService cartService;

    @Autowired
    public MenuController(PizzaService pizzaService, OrderService orderService, CartService cartService) {
        this.pizzaService = pizzaService;
        this.orderService = orderService;
        this.cartService = cartService;
    }
    @GetMapping("/")
    public String showHomePage(Model model){
        model.addAttribute("featuredPizzas",
                "sasuge pizza, devilled chicken pizza, sweet corn Pizza" +
                        "All in at LKR1800 Order Now today only"); //pizzaService.getFeaturedPizzas()
        return "home";
    }

    @GetMapping("/menu")
    public String showMenu(Model model){
        model.addAttribute("pizzas",pizzaService.getAllPizzas());
        return "menu";
    }
//    @PostMapping("/order/submit")
//    public String submitOrder(@ModelAttribute Order order) {
//        orderService.save(order);
//        return "redirect:/order/success";
//    }

    @PostMapping("/order/submit")
    public String submitOrder(@Valid @ModelAttribute OrderRequest orderRequest ,
                              BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "order-form";
        }
        orderService.createOrderFromCart(orderRequest);
        return "redirect:/order/success";
    }


}
