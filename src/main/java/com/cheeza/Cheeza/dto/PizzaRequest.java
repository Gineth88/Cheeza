package com.cheeza.Cheeza.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PizzaRequest {
    @NotBlank(message = "Pizza name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3-50 characters")
    private String name;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "5.00", message = "Minimum price is $5.00")
    @DecimalMax(value = "50.00", message = "Maximum price is $50.00")
    private double basePrice;

    @Pattern(regexp = "https?:\\/\\/.*\\.(?:png|jpg|jpeg|gif)",
            message = "Must be a valid image URL")
    private String imageUrl;

    private boolean featured;

    @NotNull(message = "Toppings selection required")
    private Set<Long> toppingIds; // IDs of selected toppings
}