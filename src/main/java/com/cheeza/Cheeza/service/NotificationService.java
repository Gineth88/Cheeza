package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.model.Notification;
import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderStatus;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Send a notification to a user and store it in the database
     */
    /*public void sendNotification(User user, String title, String message) {
        Notification notification = new Notification(title, message, LocalDateTime.now());
        notification.setUser(user);
        notification = notificationRepository.save(notification);

        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(), // Make sure this matches the user's principal name
                    "/queue/notifications",
                    Map.of(
                            "id", notification.getId(),
                            "title", notification.getTitle(),
                            "message", notification.getMessage(),
                            "createdAt", notification.getCreatedAt(),
                            "read", notification.isRead()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }*/

    public void sendNotification(User user, String title, String message) {
        log.info("Preparing notification for: {}", user.getEmail());

        Notification notification = new Notification(title, message, LocalDateTime.now());
        notification.setUser(user);
        notification = notificationRepository.save(notification);
        log.info("Notification saved with ID: {}", notification.getId());

        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail().trim().toLowerCase(),
                    "/queue/notifications",
                    Map.of(
                            "id", notification.getId(),
                            "title", title,
                            "message", message,
                            "createdAt", notification.getCreatedAt().toString(),
                            "read", false
                    )
            );
            log.info("WebSocket message sent successfully");
        } catch (Exception e) {
            log.error("Failed to send WebSocket message", e);
        }
    }


    private String getStatusTitle(OrderStatus status) {
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

    /**
     * Create detailed message for order status update
     */
    private String createStatusUpdateMessage(Order order, OrderStatus oldStatus) {
        StringBuilder message = new StringBuilder();

        // Add basic order info
        message.append("Order #").append(order.getId());

        // Add status-specific message
        message.append("\nStatus: ").append(order.getStatus());

        // Add delivery time if available
        if (order.getEstimatedDelivery() != null) {
            message.append("\nEstimated delivery: ")
                    .append(order.getEstimatedDelivery().format(TIME_FORMATTER));
        }

        return message.toString();
    }
    public void sendOrderStatusUpdate(Order order, OrderStatus oldStatus) {
        User user = order.getUser();
        String title = "Order Update: " + order.getStatus();
        String message = createStatusUpdateMessage(order, oldStatus);

        Notification notification = new Notification(title, message, LocalDateTime.now());
        notification.setUser(user);
        notification = notificationRepository.save(notification);

        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    new NotificationDTO(
                            notification.getId(),
                            notification.getTitle(),
                            notification.getMessage(),
                            notification.getCreatedAt(),
                            notification.isRead()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class NotificationDTO {
        private Long id;
        private String title;
        private String message;
        private LocalDateTime createdAt;
        private boolean read;
    }
}
