package com.cheeza.Cheeza.dto;

import com.cheeza.Cheeza.model.OrderStatus;

import java.time.Instant;

public record OrderStatusUpdate(
        Long orderId,
        OrderStatus status,
        Instant lastUpdated,
        Instant estimatedDelivery
) {}
