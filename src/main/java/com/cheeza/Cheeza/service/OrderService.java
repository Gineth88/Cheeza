package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.dto.OrderResponse;
import com.cheeza.Cheeza.exception.OrderNotFoundException;
import com.cheeza.Cheeza.exception.UserNotFoundException;
import com.cheeza.Cheeza.model.*;
import com.cheeza.Cheeza.observer.OrderObserver;
import com.cheeza.Cheeza.repository.OrderRepository;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PizzaRepository pizzaRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderObserver customerObserver;

    @Autowired(required = false) // Make optional if WebSocket isn't crucial
    private SimpMessagingTemplate messagingTemplate;

    public Order createOrder(OrderRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(request.getCustomerName());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPhone(request.getPhone());
        order.setStatus(OrderStatus.RECEIVED);
        order.setOrderTime(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));

        List<OrderItem> items = request.getItems().stream()
                .map(item -> {
                    Pizza pizza = pizzaRepository.findById(item.getPizzaId())
                            .orElseThrow(() -> new RuntimeException("Pizza not found"));
                    return new OrderItem(pizza, item.getQuantity(), item.getSpecialInstructions(), order);
                })
                .toList();

        order.setItems(items);
        order.setTotalPrice(calculateTotal(items));

        Order savedOrder = orderRepository.save(order);
        notifyOrderCreated(savedOrder);
        return savedOrder;
    }

    private double calculateTotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPizza().getBasePrice() * item.getQuantity())
                .sum();
    }

    public Order createOrderFromCart(OrderRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<OrderItem> items = convertCartItems();
        double total = cartService.getTotal();

        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.RECEIVED);
        order.setOrderTime(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();
        notifyOrderCreated(savedOrder);
        return savedOrder;
    }

    private List<OrderItem> convertCartItems() {
        return cartService.getCartItems().stream()
                .map(item -> new OrderItem(item.getPizza(), item.getQuantity()))
                .toList();
    }

    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        order.registerObserver(customerObserver);
        notifyStatusChange(order, oldStatus);
    }

    private void notifyOrderCreated(Order order) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSendToUser(
                    order.getUser().getEmail(),
                    "/queue/orders",
                    new OrderNotification(order.getId(), "Order created", OrderStatus.RECEIVED)
            );
        }
    }

    private void notifyStatusChange(Order order, OrderStatus oldStatus) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSendToUser(
                    order.getUser().getEmail(),
                    "/queue/orders",
                    new OrderNotification(order.getId(), "Status changed", order.getStatus())
            );
        }
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public Order placeOrder(OrderRequest request) {
        // Get authenticated user (you may need to adjust this based on your auth setup)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        // Convert cart items to order items
        List<OrderItem> items = convertCartItems();

        // Calculate total
        double total = items.stream()
                .mapToDouble(item -> item.getPizza().getBasePrice() * item.getQuantity())
                .sum();

        // Create and save order
        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.RECEIVED);
        order.setOrderTime(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();

        // Send notification if needed
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/orders",
                    new OrderNotification(savedOrder.getId(), "Order placed", OrderStatus.RECEIVED)
            );
        }

        return savedOrder;
    }

    // DTO for WebSocket messages
    @Data
    @AllArgsConstructor
    private static class OrderNotification {
        private Long orderId;
        private String message;
        private OrderStatus status;
    }
}