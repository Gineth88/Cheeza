package com.cheeza.Cheeza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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

    private String imageFileName;
    private boolean featured;
    private boolean available;

    @Transient
    private MultipartFile imageFile;

    private String crustType;
    private String sauceType;
    @ElementCollection
    private List<String> toppings;
    private double sizeMultiplier;

    private Double calculatedPrice;


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

    @Transient
    private Set<Topping> selectedToppings = new HashSet<>();


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






    public double getPrice(){
        double toppingCost = toppings.size()*0.5;
        return (basePrice+toppingCost)*sizeMultiplier;
    }


    public static class PizzaBuilder {
        private String name;
        private double basePrice;
        private String description = "";
        private String size = "M";
        private Set<Topping> toppings = new HashSet<>();
        private String imageFileName;
        private boolean available = true;
        private String crustType = "Regular";
        private String sauceType = "Tomato";
        private double sizeMultiplier = 1.0;

        public PizzaBuilder(String name, double basePrice) {
            this.name = name;
            this.basePrice = basePrice;
        }

        public PizzaBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PizzaBuilder size(String size) {
            this.size = size;
            return this;
        }

        public PizzaBuilder topping(Topping topping) {
            this.toppings.add(topping);
            return this;
        }

        public PizzaBuilder imageFileName(String imageFileName) {
            this.imageFileName = imageFileName;
            return this;
        }

        public PizzaBuilder crustType(String crustType) {
            this.crustType = crustType;
            return this;
        }

        public PizzaBuilder sauceType(String sauceType) {
            this.sauceType = sauceType;
            return this;
        }

        public PizzaBuilder sizeMultiplier(double sizeMultiplier) {
            this.sizeMultiplier = sizeMultiplier;
            return this;
        }

        public Pizza build() {
            Pizza pizza = new Pizza();
            pizza.setName(name);
            pizza.setBasePrice(basePrice);
            pizza.setDescription(description);
            pizza.setSize(size);
            pizza.setAvailableToppings(toppings);
            pizza.setImageFileName(imageFileName);
            pizza.setAvailable(available);
            pizza.setCrustType(crustType);
            pizza.setSauceType(sauceType);
            pizza.setSizeMultiplier(sizeMultiplier);
            return pizza;
        }
    }

    public static PizzaBuilder builder(String name, double basePrice) {
        return new PizzaBuilder(name, basePrice);
    }
}

