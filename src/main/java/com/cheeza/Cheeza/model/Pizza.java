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

    public static PizzaBuilder builder(String name, double basePrice){
        return new PizzaBuilder(name,basePrice);

    }

    public static class PizzaBuilder{
        private final String name;
        private final double basePrice;
        private String description = "";
        private String size = "M";
        private Set<Topping> toppings = new HashSet<>();
        private String imageFileName;

        public PizzaBuilder (String name,double basePrice){
            this.name = name;
            this.basePrice = basePrice;
        }
        public PizzaBuilder description(String description){
            this.description = description;
            return this;
        }
        public PizzaBuilder size(String size){
            this.size = size;
            return this;
        }
        public PizzaBuilder topping(Topping topping){
            this.toppings.add(topping);
            return this;
        }
        public PizzaBuilder imageFileName(String imageFileName) {
            this.imageFileName = imageFileName;
            return this;
        }


        public Pizza build(){
            Pizza pizza = new Pizza();
            pizza.setName(name);
            pizza.setBasePrice(basePrice);
            pizza.setDescription(description);
            pizza.setSize(size);
            pizza.setAvailableToppings(toppings);
            pizza.setImageFileName(imageFileName);
            pizza.setAvailable(true);
            return pizza;
        }
    }
}
