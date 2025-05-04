package com.cheeza.Cheeza.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderItemRequest {
    private Long pizzaId;
    private int quantity;
    private String specialInstructions;
    private List<Long> toppingIds;
}
