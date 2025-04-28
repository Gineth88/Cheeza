package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.OrderStatusUpdate;
import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.repository.OrderRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class OrderTrackingService {
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderRepository orderRepository;

    public OrderTrackingService(SimpMessagingTemplate messagingTemplate, OrderRepository orderRepository){
        this.messagingTemplate = messagingTemplate;
        this.orderRepository = orderRepository;
    }

    public void notifyOrderStatusChange (Long id){

        Order order = orderRepository.findById(id)
                .orElseThrow();

        messagingTemplate.convertAndSend(
                "/topic/order-status/" + id,
                new OrderStatusUpdate(
                        order.getId(),
                        order.getStatus(),
                        order.getLastUpdated(),
                        order.getEstimatedDelivery()
                )
        );
    }
}
