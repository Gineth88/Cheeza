package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.dto.OrderResponse;
import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderItem;
import com.cheeza.Cheeza.model.OrderStatus;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.repository.OrderRepository;
import com.cheeza.Cheeza.repository.PizzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private CartService cartService;

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
}
