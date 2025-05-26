
package com.cheeza.Cheeza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;
    private double price;
    private boolean vegetarian;

    // Set a default value of 0.0
    @Column(name = "additional_price", nullable = false, columnDefinition = "double default 0.0")
    private Double additionalPrice = 0.0;

    private String description;

    // Add explicit constructor for initialization
    public Topping(String name, double price) {
        this.name = name;
        this.price = price;
        this.vegetarian = false; // default value
        this.additionalPrice = price; // Set additional price to match price
    }

    @ManyToMany(mappedBy = "availableToppings")
    @JsonIgnore
    private Set<Pizza> pizzas = new HashSet<>();

    public void addToPizza(Pizza pizza) {
        this.pizzas.add(pizza);
        pizza.getAvailableToppings().add(this);
    }

    // Helper method to remove from pizza
    public void removeFromPizza(Pizza pizza) {
        this.pizzas.remove(pizza);
        pizza.getAvailableToppings().remove(this);
    }

    // Ensure getAdditionalPrice never returns null
    public Double getAdditionalPrice() {
        return additionalPrice != null ? additionalPrice : price;
    }
}
