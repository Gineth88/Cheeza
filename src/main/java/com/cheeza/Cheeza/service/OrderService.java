
package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.CartItem;
import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.dto.OrderResponse;
import com.cheeza.Cheeza.exception.OrderNotFoundException;
import com.cheeza.Cheeza.exception.PaymentException;
import com.cheeza.Cheeza.exception.UserNotFoundException;
import com.cheeza.Cheeza.model.*;
import com.cheeza.Cheeza.observer.OrderObserver;
import com.cheeza.Cheeza.repository.OrderRepository;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final PizzaRepository pizzaRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderObserver customerObserver;
    private  final UserLoyaltyService userLoyaltyService;
    private final NotificationService notificationService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Autowired(required = false)
    private PaymentService paymentService;

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

        // Register observer before saving
        order.registerObserver(customerObserver);
        Order savedOrder = orderRepository.save(order);

        notifyOrderCreated(savedOrder);
        return savedOrder;
    }


    private double calculateTotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPizza().getBasePrice() * item.getQuantity())
                .sum();
    }

    public Order createOrderFromCart(OrderRequest request, HttpSession session) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<OrderItem> items = convertCartItems(session);
        double total = cartService.getTotal(session);

        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.RECEIVED);
        order.setOrderTime(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(session);
        notifyOrderCreated(savedOrder);
        return savedOrder;
    }


    private List<OrderItem> convertCartItems(HttpSession session) {
        List<CartItem> cartItems = cartService.getCartItems(session);
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            try {
                Pizza pizza = pizzaRepository.findById(item.getPizzaId())
                        .orElseThrow(() -> new RuntimeException("Pizza not found with ID: " + item.getPizzaId()));

                OrderItem orderItem = new OrderItem(pizza, item.getQuantity());
                orderItems.add(orderItem);
            } catch (Exception e) {
                log.error("Error converting cart item to order item", e);
            }
        }

        return orderItems;
    }

    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        // This will notify all registered observers
        order.notifyObservers(order,oldStatus);

        // WebSocket update
        sendWebSocketUpdate(order, "Status changed to " + newStatus);
    }

    private void sendWebSocketUpdate(Order order, String message) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSendToUser(
                    order.getUser().getEmail(),
                    "/queue/notifications", // Changed from /queue/orders to /queue/notifications
                    Map.of(
                            "orderId", order.getId(),
                            "status", order.getStatus().name(),
                            "message", message,
                            "title", "Order Update",
                            "createdAt", LocalDateTime.now().toString()
                    )
            );
        }
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

    public Order placeOrder(OrderRequest request, HttpSession session, String paymentMethod) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        List<OrderItem> items = convertCartItems(session);
        double total = items.stream().mapToDouble(item -> item.getPizza().getBasePrice() * item.getQuantity()).sum();

        // If the paymentMethod is "LoyaltyPoints", convert amount to points, redeem them:
        if (paymentMethod.equalsIgnoreCase("LoyaltyPoints")) {
            int pointsNeeded = (int) (total * 100); // see LoyaltyPointsPayment (100 points = 1 LKR)
            boolean success = userLoyaltyService.redeemPoints(user, pointsNeeded);
            if (!success) throw new RuntimeException("Insufficient loyalty points.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.RECEIVED);
        order.setOrderTime(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(session);

        // Only award points if not redeeming
        if (!paymentMethod.equalsIgnoreCase("LoyaltyPoints")) {
            userLoyaltyService.awardPoints(user, total);
        }

        return savedOrder;
    }

    public Order checkout(Order order, String paymentMethod) {
        if (paymentService == null) {
            log.warn("PaymentService is not available");
            order.setStatus(OrderStatus.RECEIVED);
            return order;
        }

        double total = order.calculateTotal();
        boolean paymentSuccess = paymentService.processPayment(paymentMethod, total);

        if (paymentSuccess) {
            order.setStatus(OrderStatus.PAID);
            notificationService.sendOrderStatusUpdate(order, OrderStatus.RECEIVED);
            return order;
        }
        throw new PaymentException("Payment processing failed");
    }

    public void processPayment(Order order, String paymentMethod) throws PaymentException {
        double total = order.calculateTotal();

        try {
            boolean paymentSuccess = mockPaymentProcessing(total, paymentMethod);

            if (!paymentSuccess) {
                throw new PaymentException("Payment failed for order " + order.getId());
            }

            order.setStatus(OrderStatus.PAID);

        } catch (Exception e) {
            throw new PaymentException("Error processing payment", e);
        }
    }

    private boolean mockPaymentProcessing(double amount, String paymentMethod) {
        log.info("Processing {} payment of ${}", paymentMethod, amount);
        return true;
    }


    @Data
    @AllArgsConstructor
    private static class OrderNotification {
        private Long orderId;
        private String message;
        private OrderStatus status;
    }
}
