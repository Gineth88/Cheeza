package com.cheeza.Cheeza.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double basePrice;
    private String size; // S,M,L,XL

    public Pizza(String name, String description, Double basePrice,
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
}
