package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.dto.OrderResponse;
import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request){
        return orderService.createOrder(request);
    }
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id){
        return orderService.getOrder(id);
    }

    @GetMapping
    public String showOrderForm(Model model) {
        model.addAttribute("orderRequest", new OrderRequest());
        return "order";
    }

//    @PostMapping
//    public String submitOrder(@ModelAttribute OrderRequest orderRequest) {
//        orderService.createOrderFromCart(orderRequest);
//        return "redirect:/order/success";
//    }
}
