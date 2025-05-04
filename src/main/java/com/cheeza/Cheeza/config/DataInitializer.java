package com.cheeza.Cheeza.config;

import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.ToppingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;

@Configuration
public class DataInitializer {

   /* @Bean
    CommandLineRunner initMenu(PizzaRepository pizzaRepository, ToppingRepository toppingRepository) {
        return args -> {
            // Clear existing data
            pizzaRepository.deleteAll();
            toppingRepository.deleteAll();

            // Create and save toppings
            Topping cheese = Topping.builder()
                    .name("Mozzarella Cheese")
                    .price(1.5)
                    .vegetarian(true)
                    .iconClass("fas fa-cheese")
                    .build();

            Topping pepperoni = Topping.builder()
                    .name("Pepperoni")
                    .price(2.0)
                    .vegetarian(false)
                    .iconClass("fas fa-pepper-hot")
                    .build();

            Topping mushrooms = Topping.builder()
                    .name("Mushrooms")
                    .price(1.0)
                    .vegetarian(true)
                    .iconClass("fas fa-mushroom")
                    .build();

            List<Topping> savedToppings = toppingRepository.saveAll(List.of(
                    cheese,
                    pepperoni,
                    mushrooms
            ));

            // Create and save pizzas
            Pizza margherita = Pizza.builder()
                    .name("Margherita")
                    .description("Classic tomato sauce and mozzarella")
                    .basePrice(10.99)
                    .size("M")
                    .available(true)
                    .imageUrl("https://images.unsplash.com/photo-1574071318508-1cdbab80d002")
                    .featured(true)
                    .availableToppings(new HashSet<>(List.of(cheese)))
                    .build();

            Pizza pepperoniPizza = Pizza.builder()
                    .name("Pepperoni Special")
                    .description("Loaded with double pepperoni")
                    .basePrice(14.99)
                    .size("L")
                    .available(true)
                    .imageUrl("https://images.unsplash.com/photo-1628840042765-356cda07504e")
                    .featured(false)
                    .availableToppings(new HashSet<>(List.of(cheese, pepperoni)))
                    .build();

            Pizza veggie = Pizza.builder()
                    .name("Veggie Delight")
                    .description("Mixed fresh vegetables")
                    .basePrice(12.99)
                    .size("M")
                    .available(true)
                    .imageUrl("https://images.unsplash.com/photo-1595854341625-f33ee10dbf94")
                    .featured(true)
                    .availableToppings(new HashSet<>(List.of(cheese, mushrooms)))
                    .build();

            pizzaRepository.saveAll(List.of(margherita, pepperoniPizza, veggie));
        };
    }*/
}