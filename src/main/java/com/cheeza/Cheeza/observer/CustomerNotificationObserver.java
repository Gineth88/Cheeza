package com.cheeza.Cheeza.observer;

import com.cheeza.Cheeza.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerNotificationObserver implements OrderObserver {

    @Override
    public void update(Order order, OrderStatus orderStatus){
        String message = createNotificationMessage(order);
        sendNotification(order.getUser(),message);
    }

    private String createNotificationMessage(Order order){
        long minutesPassed = ChronoUnit.MINUTES.between(
                order.getOrderTime(),
                LocalDateTime.now()
        );
        long remaining  = 30-minutesPassed;

        return String.format(
                "Order #%d Status: %s\n" +
                        "Items: %s\n" +
                        "Total: $%.2f\n" +
                        "Time Remaining: ~%d minutes",
                order.getId(),
                order.getStatus(),
                formatItems(order.getItems()),
                order.getTotalPrice(),
                remaining
        );

    }
    private String formatItems(List<OrderItem> items) {
        return items.stream()
                .map(item -> String.format("%s x%d",
                        item.getPizza().getName(),
                        item.getQuantity()))
                .collect(Collectors.joining(", "));
    }
    private void sendNotification(User user, String message) {
        // Store in database for in-app notifications
        Notification notification = new Notification(
                "Order Update",
                message,
                LocalDateTime.now()
        );
        user.addNotification(notification);
    }

}
