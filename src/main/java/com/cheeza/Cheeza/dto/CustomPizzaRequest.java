package com.cheeza.Cheeza.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CustomPizzaRequest {
    @NotNull
    private Long pizzaId;

    @NotNull
    private String size;

    @NotNull
    private String crustType;

    @NotNull
    private String sauceType;

    private List<Long> selectedToppingIds = new ArrayList<>();

    @Min(1)
    @Max(10)
    private int quantity = 1;
}
