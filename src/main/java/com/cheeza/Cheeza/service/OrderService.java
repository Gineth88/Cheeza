package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.dto.OrderResponse;
import com.cheeza.Cheeza.exception.OrderNotFoundException;
import com.cheeza.Cheeza.exception.UserNotFoundException;
import com.cheeza.Cheeza.model.*;
import com.cheeza.Cheeza.repository.OrderRepository;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public OrderService(OrderRepository orderRepository,SimpMessagingTemplate messagingTemplate) {
       this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Order createOrder(OrderRequest request){
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPhone(request.getPhone());
        order.setEmail(request.getEmail());
        order.setStatus(OrderStatus.RECEIVED);
        order.setOrderTime(LocalDateTime.now());

        List<OrderItem> items = request.getItems().stream()
                .map(item -> {
                    Pizza pizza = pizzaRepository.findById(item.getPizzaId())
                            .orElseThrow(() -> new RuntimeException("Pizza not found"));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setPizza(pizza);
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setSpecialInstruction(item.getSpecialInstructions());
                    orderItem.setOrder(order); // Bidirectional relationship
                    return orderItem;
                })
                .collect(Collectors.toList());

                order.setItems(items);

        double total = items.stream()
                .mapToDouble(item -> item.getPizza().getBasePrice() * item.getQuantity())
                .sum();
        order.setTotalPrice(total);

               return orderRepository.save(order);

    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getItems().stream()
                        .map(item -> new OrderResponse.OrderItemResponse(
                                item.getPizza().getName(),
                                item.getQuantity()
                        ))
                        .collect(Collectors.toList())
        );
    }
    private List<OrderItem> convertCartItems() {
        return cartService.getCartItems().stream()
                .map(cartItem -> new OrderItem(cartItem.getPizza(), cartItem.getQuantity()))
                .collect(Collectors.toList());
    }
    public Order createOrderFromCart(OrderRequest request){
        List<OrderItem> items = convertCartItems();


        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPhone(request.getPhone());
        order.setEmail(request.getEmail());
        order.setStatus(OrderStatus.RECEIVED);
        order.setOrderTime(LocalDateTime.now());
        order.setItems(items);

        order.setTotalPrice(cartService.getTotal());
        Order saveOrder = orderRepository.save(order);
        cartService.clearCart();
        return saveOrder;
    }

    public Order placeOrder(Long userId, List<OrderItem> items) {
        // 1. Get the user first
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. Create and save the order
        Order order = new Order();
        order.setUser(user); // Set the user before saving
        order.setItems(items);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(OrderStatus.RECEIVED);
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));

        Order savedOrder = orderRepository.save(order);

        // 3. Send notification
        sendOrderUpdate(savedOrder);
        return savedOrder;
    }




    public void updateOrderStatus(Long orderId,OrderStatus newStatus){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new OrderNotFoundException(orderId));

        order.setStatus(newStatus);
        orderRepository.save(order);

        sendOrderUpdate(order);

    }
    private void sendOrderUpdate(Order order){
        messagingTemplate.convertAndSendToUser(
                order.getUser().getEmail(),
                "/qeue/orders/",
                order
        );
    }
}
