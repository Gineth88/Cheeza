
package com.cheeza.Cheeza.service.sheduler;

import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderStatus;
import com.cheeza.Cheeza.repository.OrderRepository;
import com.cheeza.Cheeza.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoOrderStatusScheduler {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    /**
     * Simulate rapid status changes for an order with notifications (for demo).
     */
    @Async // run in a separate thread
    public void startDemoTimer(Order order) {
        try {
            // If order is not yet paid, abort demo timer
            if (order.getStatus() != OrderStatus.PAID) return;

            // 1. PAID -> PREPARING (after 20s)
            Thread.sleep(20 * 1000L);
            updateOrderStatus(order, OrderStatus.PREPARING);

            // 2. PREPARING -> BAKING (after 10s)
            Thread.sleep(10 * 1000L);
            updateOrderStatus(order, OrderStatus.BAKING);

            // 3. BAKING -> READY (after 90s)
            Thread.sleep(90 * 1000L);
            updateOrderStatus(order, OrderStatus.READY);

            // 4. READY -> OUT_FOR_DELIVERY (after 30s)
            Thread.sleep(30 * 1000L);
            updateOrderStatus(order, OrderStatus.OUT_FOR_DELIVERY);

            // 5. OUT_FOR_DELIVERY -> DELIVERED (after 20s for demo)
            Thread.sleep(20 * 1000L);
            updateOrderStatus(order, OrderStatus.DELIVERED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        Order refreshed = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Order not found during status update"));
        OrderStatus oldStatus = refreshed.getStatus();
        refreshed.setStatus(newStatus);
        orderRepository.save(refreshed);

        // Notify the user
        notificationService.sendOrderStatusUpdate(refreshed, oldStatus);
    }
}
