
package com.cheeza.Cheeza.observer;

import com.cheeza.Cheeza.model.*;
import com.cheeza.Cheeza.repository.NotificationRepository;
import com.cheeza.Cheeza.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CustomerNotificationObserver implements OrderObserver {
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    Logger log = LoggerFactory.getLogger(CustomerNotificationObserver.class);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

//    @Override
//    public void update(Order order, OrderStatus oldStatus) {
//        String title = getNotificationTitle(order.getStatus());
//        String message = createNotificationMessage(order, oldStatus);
//
//        // Create and persist notification
//        Notification notification = new Notification(title, message, LocalDateTime.now());
//        notification.setUser(order.getUser());
//        notificationRepository.save(notification);
//
//        // Send via WebSocket
//        notificationService.sendNotification(order.getUser(), title, message);
//    }

    private String getNotificationTitle(OrderStatus status) {
        return switch (status) {
            case RECEIVED -> "Order Received";
            case PREPARING -> "Order Preparation Started";
            case BAKING -> "Your Pizza is Baking";
            case READY -> "Your Order is Ready";
            case OUT_FOR_DELIVERY -> "Order Out for Delivery";
            case DELIVERED -> "Order Delivered";
            case PAID -> "Payment Confirmed";
            case CANCELLED -> "Order Cancelled";
            default -> "Order Update";
        };
    }

    private String createNotificationMessage(Order order, OrderStatus oldStatus) {
        // Calculate estimated delivery time
        long minutesRemaining = calculateRemainingTime(order);

        // Get status-specific message
        String statusMessage = getStatusSpecificMessage(order.getStatus(), minutesRemaining);

        // Format basic order info
        String orderInfo = formatOrderInfo(order);

        // Combine all information
        return String.format("%s\n\n%s\n\nOrder #%d\n%s\nTotal: $%.2f",
                statusMessage,
                getStatusProgressBar(order.getStatus()),
                order.getId(),
                orderInfo,
                order.getTotalPrice()
        );
    }

    private String getStatusSpecificMessage(OrderStatus status, long minutesRemaining) {
        return switch (status) {
            case RECEIVED -> String.format(
                    "We've received your order! We'll start preparing it right away. " +
                            "Estimated delivery time: ~%d minutes.",
                    minutesRemaining);

            case PREPARING -> String.format(
                    "Our chefs have started preparing your delicious pizza. " +
                            "Estimated delivery time: ~%d minutes.",
                    minutesRemaining);

            case BAKING -> String.format(
                    "Your pizza is now in the oven! It's getting all hot and cheesy. " +
                            "Estimated delivery time: ~%d minutes.",
                    minutesRemaining);

            case READY -> String.format(
                    "Your order is ready! We're getting it packaged for delivery. " +
                            "Estimated delivery time: ~%d minutes.",
                    minutesRemaining);

            case OUT_FOR_DELIVERY -> String.format(
                    "Your order is on its way to you! " +
                            "Estimated arrival: ~%d minutes.",
                    minutesRemaining);

            case DELIVERED -> "Your order has been delivered. Enjoy your meal!";

            case PAID -> "Thank you for your payment. Your order is confirmed.";

            case CANCELLED -> "Your order has been cancelled. " +
                    "Please contact customer service if this was a mistake.";

            default -> String.format(
                    "Your order status has been updated. " +
                            "Estimated delivery time: ~%d minutes.",
                    minutesRemaining);
        };
    }

    private String getStatusProgressBar(OrderStatus status) {
        // Visual representation of order progress
        String[] stages = {"RECEIVED", "PREPARING", "BAKING", "READY", "OUT_FOR_DELIVERY", "DELIVERED"};
        int currentStageIndex = -1;

        // Find current stage index
        for (int i = 0; i < stages.length; i++) {
            if (stages[i].equals(status.name())) {
                currentStageIndex = i;
                break;
            }
        }

        // If status is not in regular flow (PAID or CANCELLED)
        if (currentStageIndex == -1) {
            if (status == OrderStatus.PAID) {
                return "✅ Payment Confirmed";
            } else if (status == OrderStatus.CANCELLED) {
                return "❌ Order Cancelled";
            } else {
                return ""; // Unknown status
            }
        }

        // Build progress bar
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < stages.length; i++) {
            if (i < currentStageIndex) {
                progressBar.append("✅ ").append(stages[i]).append(" → ");
            } else if (i == currentStageIndex) {
                progressBar.append("🔶 ").append(stages[i]).append(" → ");
            } else {
                progressBar.append("⚪ ").append(stages[i]);
                if (i < stages.length - 1) {
                    progressBar.append(" → ");
                }
            }
        }

        return progressBar.toString();
    }

    private long calculateRemainingTime(Order order) {
        // Calculate remaining time based on estimated delivery time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime estimatedDelivery = order.getEstimatedDelivery();

        if (estimatedDelivery == null) {
            return 30; // Default 30 minutes if no estimated time
        }

        long minutesRemaining = ChronoUnit.MINUTES.between(now, estimatedDelivery);
        return Math.max(0, minutesRemaining); // Ensure non-negative
    }

    private String formatOrderInfo(Order order) {
        // Format the order items
        StringBuilder builder = new StringBuilder();
        List<OrderItem> items = order.getItems();

        if (items != null && !items.isEmpty()) {
            builder.append("Items: ");
            builder.append(items.stream()
                    .map(item -> String.format("%s x%d",
                            item.getPizza() != null ? item.getPizza().getName() : "Custom Pizza",
                            item.getQuantity()))
                    .collect(Collectors.joining(", ")));
        }

        LocalDateTime estimatedDelivery = order.getEstimatedDelivery();
        if (estimatedDelivery != null) {
            builder.append("\nEstimated delivery: ");
            builder.append(estimatedDelivery.format(TIME_FORMATTER));
        }

        return builder.toString();
    }

    private void sendNotification(User user, String title, String message) {
        if (user == null) {
            return;
        }
        Notification notification = new Notification(
                title,
                message,
                LocalDateTime.now()
        );

        user.addNotification(notification);
    }
    @Transactional
    public void update(Order order, OrderStatus oldStatus) {
        log.info("Observer triggered for order {} status change from {} to {}",
                order.getId(), oldStatus, order.getStatus());

        Notification notification = new Notification(
                "Order Update",
                "Status changed to " + order.getStatus(),
                LocalDateTime.now()
        );
        notification.setUser(order.getUser());

        // Explicitly save and flush
        notification = notificationRepository.saveAndFlush(notification);
        log.info("Notification saved with ID: {}", notification.getId());

        notificationService.sendNotification(order.getUser(),
                "Order Update",
                "Status changed to " + order.getStatus());
    }
}
