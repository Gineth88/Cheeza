package com.cheeza.Cheeza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


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

    private String imageFileName; // For creative UI
    private boolean featured;
    private boolean available;

    @Transient
    private MultipartFile imageFile;


    public Pizza(String name, String description, double basePrice, Set<Topping> availableToppings) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.size = "M";
        this.available = true;
        this.availableToppings = availableToppings;

    }
    public Pizza(String name, String description, double basePrice) {
        this(name, description, basePrice, new HashSet<>());
    }



    @ManyToMany
    @JoinTable(
            name = "pizza_toppings",
            joinColumns = @JoinColumn(name = "pizza_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id")
    )
    private Set<Topping> availableToppings = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        if (this.availableToppings == null) {
            this.availableToppings = new HashSet<>();
        }
    }

    @JsonIgnore
    public MultipartFile getImageFile() {
        return imageFile;
    }
}
