package com.cheeza.Cheeza.dto;

import com.cheeza.Cheeza.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String customerName;
    private List<OrderItemResponse>items;

    @Data
    @AllArgsConstructor
    public static class OrderItemResponse{
        private String pizzaName;
        private int quantity;
    }
}
