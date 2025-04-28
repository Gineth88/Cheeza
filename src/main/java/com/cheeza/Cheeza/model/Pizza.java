package com.cheeza.Cheeza.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double basePrice;
    private String size; // S,M,L,XL

    public Pizza(String name, String description, double basePrice,
                  List<String> toppings) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.size = "M";
        this.available = true;
        this.toppings = toppings;
    }

    private boolean available;

    @ElementCollection
    private List<String>toppings;

    @ManyToMany
    @JoinTable(
            name = "pizza_toppings",
            joinColumns = @JoinColumn(name = "pizza_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id")
    )
    private Set<Topping> availableToppings = new HashSet<>();

    private String imageUrl; // For creative UI
    private boolean featured;

    @PrePersist
    protected void onCreate() {
        if (this.availableToppings == null) {
            this.availableToppings = new HashSet<>();
        }
    }
}
