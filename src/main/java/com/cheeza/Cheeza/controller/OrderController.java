package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.dto.OrderResponse;
import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderItem;
import com.cheeza.Cheeza.model.OrderStatus;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return convertToResponse(order);
    }

    @PostMapping("/submit")
    public String submitOrder(@ModelAttribute OrderRequest orderRequest) {
        orderService.createOrderFromCart(orderRequest);
        return "redirect:/order/success";
    }

    @GetMapping("/track/{orderId}")
    public String trackOrder(@PathVariable Long orderId, Model model) {
        OrderResponse order = convertToResponse(orderService.getOrder(orderId));
        model.addAttribute("order", order);
        return "order-tracking";
    }

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(
            @AuthenticationPrincipal User user,
            @RequestBody OrderRequest orderRequest
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        orderRequest.setEmail(user.getEmail());
        return ResponseEntity.ok(orderService.placeOrder(orderRequest));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    private OrderResponse convertToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getItems().stream()
                        .map(item -> new OrderResponse.OrderItemResponse(
                                item.getPizza().getName(),
                                item.getQuantity()
                        ))
                        .toList()
        );
    }
}
