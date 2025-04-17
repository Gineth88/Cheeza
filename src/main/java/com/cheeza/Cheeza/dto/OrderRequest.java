package com.cheeza.Cheeza.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private String customerName;
    private String deliveryAddress;
    private String phone;
    private String email;
    private List<OrderItemRequest> items;
}

