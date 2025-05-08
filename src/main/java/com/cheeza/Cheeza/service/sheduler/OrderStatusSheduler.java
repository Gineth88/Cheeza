package com.cheeza.Cheeza.service.sheduler;

import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderStatus;
import com.cheeza.Cheeza.repository.OrderRepository;
import com.cheeza.Cheeza.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusSheduler {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void autoUpdateOrderStatuses() {
        List<Order> activeOrders = orderRepository.findActiveOrders(
                List.of(OrderStatus.RECEIVED,OrderStatus.CANCELLED)
        );

        activeOrders.forEach(order -> {
            long minutesPassed = ChronoUnit.MINUTES.between(
                    order.getOrderTime(),
                    LocalDateTime.now()
            );

            OrderStatus newStatus = calculateNewStatus(minutesPassed);
            if(newStatus != order.getStatus()) {
                orderService.updateOrderStatus(order.getId(), newStatus);
            }
        });
    }

    private OrderStatus calculateNewStatus(long minutesPassed) {
        return switch ((int) minutesPassed){
            case 0 -> OrderStatus.RECEIVED;
            case 5 -> OrderStatus.PREPARING;
            case 10 -> OrderStatus.BAKING;
            case 15 -> OrderStatus.READY;
            case 20 -> OrderStatus.OUT_FOR_DELIVERY;
            case 25 -> OrderStatus.DELIVERED;
            default -> OrderStatus.RECEIVED;
        };
    }
}
