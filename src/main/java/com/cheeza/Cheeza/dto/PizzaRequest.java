package com.cheeza.Cheeza.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PizzaRequest {
    private Long id; // For edit operations

    @NotBlank(message = "Pizza name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3-50 characters")
    private String name;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "1000.00", message = "Minimum price is LKR1000")
    @DecimalMax(value = "5000.00", message = "Maximum price is LKR5000")
    private double basePrice;

    // Image handling
    private String imageFileName;
    private MultipartFile imageFile;

    // Customization options
    @Builder.Default
    @NotNull(message = "Available sizes are required")
    private List<String> availableSizes = List.of("S", "M", "L", "XL");

    @Builder.Default
    @NotNull(message = "Crust types are required")
    private List<String> availableCrustTypes = List.of("Regular", "Thin", "Thick", "Stuffed");

    @Builder.Default
    @NotNull(message = "Sauce types are required")
    private List<String> availableSauceTypes = List.of("Tomato", "BBQ", "Alfredo", "Pesto");

    // Helper methods
    public boolean hasImageUpload() {
        return imageFile != null && !imageFile.isEmpty();
    }

    public boolean isNewPizza() {
        return id == null;
    }

    // Nested class for topping selection
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingSelection {
        private Long toppingId;
        private boolean selected;
        private double additionalPrice;
    }
}