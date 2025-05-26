package com.cheeza.Cheeza.dto;

import com.cheeza.Cheeza.model.Pizza;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CartItem implements Serializable {
    private Long pizzaId;
    private String pizzaName;
    private String pizzaDescription;
    private double basePrice;
    private String imageFileName;

    private int quantity;
    private String size;
    private String crustType;
    private String sauceType;
    private Set<ToppingDto> selectedToppings;


    public CartItem() {
        this.selectedToppings = new HashSet<>();
    }

    public CartItem(Pizza pizza, int quantity) {
        this.pizzaId = pizza.getId();
        this.pizzaName = pizza.getName();
        this.pizzaDescription = pizza.getDescription();
        this.basePrice = pizza.getBasePrice();
        this.imageFileName = pizza.getImageFileName();

        this.quantity = quantity;
        this.size = pizza.getSize() != null ? pizza.getSize() : "M";
        this.crustType = pizza.getCrustType() != null ? pizza.getCrustType() : "Regular";
        this.sauceType = pizza.getSauceType() != null ? pizza.getSauceType() : "Tomato";

        this.selectedToppings = pizza.getSelectedToppings() != null ?
                pizza.getSelectedToppings().stream()
                        .map(topping -> new ToppingDto(
                                topping.getId(),
                                topping.getName(),
                                topping.getAdditionalPrice() != null ? topping.getAdditionalPrice() : 0.0))
                        .collect(Collectors.toSet()) :
                new HashSet<>();
    }

    public double getTotalPrice() {
    // Compute price using CartItem's fields: basePrice, selectedToppings, size, quantity, etc.
    double base = this.basePrice;
    double toppingsPrice = this.selectedToppings.stream()
        .mapToDouble(t -> t.getAdditionalPrice())
        .sum();

    double multiplier = 1.0;
    if (this.size != null) {
        switch (this.size) {
            case "S": multiplier = 0.8; break;
            case "M": multiplier = 1.0; break;
            case "L": multiplier = 1.2; break;
            case "XL": multiplier = 1.5; break;
        }
    }

    return (base * multiplier + toppingsPrice) * quantity;
}

//    private double getSizeMultiplier(String size) {
//        return switch (size) {
//            case "S" -> 0.8;
//            case "L" -> 1.2;
//            case "XL" -> 1.5;
//            default -> 1.0; // Medium is the default
//        };
//    }


    public boolean matchesCustomization(CartItem other) {
        return this.size.equals(other.size) &&
                this.crustType.equals(other.crustType) &&
                this.sauceType.equals(other.sauceType) &&
                this.selectedToppings.equals(other.selectedToppings);
    }

    public boolean matches(CartItem other) {
        return this.pizzaId.equals(other.pizzaId) &&
                this.size.equals(other.size) &&
                this.crustType.equals(other.crustType) &&
                this.sauceType.equals(other.sauceType) &&
                this.selectedToppings.equals(other.selectedToppings);
    }

    public void addQuantity(int additional) {
        this.quantity += additional;
    }

    @Data
    @AllArgsConstructor
    public static class ToppingDto implements Serializable {
        private Long id;
        private String name;
        private double additionalPrice;
    }
}