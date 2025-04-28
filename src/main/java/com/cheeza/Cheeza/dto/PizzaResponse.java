package com.cheeza.Cheeza.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PizzaResponse {
    private Long id;
    private String name;
    private String description;
    private String formattedPrice;
    private String imageUrl;
    private boolean featured;
    private List<ToppingInfo> availableToppings;

    @Data
    @Builder
    public static class ToppingInfo {
        private Long id;
        private String name;
        private String iconClass;
        private boolean vegetarian;
    }
}