package com.cheeza.Cheeza.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long pizzaId;
    private int quantity;
    private String specialInstructions;
}
