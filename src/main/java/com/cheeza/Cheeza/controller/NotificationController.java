
package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.model.Notification;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.NotificationRepository;
import com.cheeza.Cheeza.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Mark all notifications as read for the current user
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        // Mark all notifications as read
        List<Notification> notifications = user.getNotifications();
        if (notifications != null) {
            notifications.forEach(notification -> {
                notification.setRead(true);
            });
            // Save changes
            notificationRepository.saveAll(notifications);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
    }

    /**
     * Mark a specific notification as read
     */
    @PostMapping("/{id}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        // Find notification
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Notification not found"));
        }

        // Verify ownership
        if (notification.getUser() == null || !notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Notification does not belong to user"));
        }

        // Mark as read
        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Get all notifications for the current user
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        User user = userService.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        List<Notification> notifications = user.getNotifications();
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notification count (unread) for the current user
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        long unreadCount = user.getNotifications().stream()
                .filter(notification -> !notification.isRead())
                .count();

        return ResponseEntity.ok(Map.of("count", unreadCount));
    }
    @PostMapping("/mark-as-read")
    public ResponseEntity<?> markNotificationsAsRead(@AuthenticationPrincipal User user) {
        List<Notification> unread = notificationRepository.findByUserAndReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok().build();
    }
}
