package com.cheeza.Cheeza.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToppingResponse {
    private Long id;
    private String name;
    private String formattedPrice;
    private boolean vegetarian;
    private String iconClass;
}
