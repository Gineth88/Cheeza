package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.Pizza;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private final PizzaRepository pizzaRepository;

    public DataLoader(PizzaRepository pizzaRepository){
        this.pizzaRepository=pizzaRepository;
    }

    @Override
    public void run(String... args)throws Exception{
        //only load data if database is empty
        if(pizzaRepository.count()==0){
//            Pizza margherita = new Pizza();
//            margherita.setName("Margherita");
//            margherita.setDescription("Classic tomato and mozerella");
//            margherita.setBasePrice(8.99);
//            margherita.setSize("M");
//            margherita.setAvailable(true);
//            margherita.setToppings(Arrays.asList("Tomato", "Mozzarella", "Basil"));
//
//            Pizza pepperoni = new Pizza();
//            pepperoni.setName("Pepperoni");
//            pepperoni.setDescription("Tomato, mozzarella and spicy pepperoni");
//            pepperoni.setBasePrice(10.99);
//            pepperoni.setSize("M");
//            pepperoni.setAvailable(true);
//            pepperoni.setToppings(Arrays.asList("Tomato", "Mozzarella", "Pepperoni"));
//
//            pizzaRepository.save(margherita);
//            pizzaRepository.save(pepperoni);

            List<Pizza> pizzas = Arrays.asList(
                    new Pizza("Margherita", "Classic tomato and mozzarella", 8.99, Arrays.asList("Tomato", "Mozzarella", "Basil")),
                    new Pizza("Pepperoni", "Spicy pepperoni with extra cheese", 10.99, Arrays.asList("Tomato", "Mozzarella", "Pepperoni")),
                    new Pizza("BBQ Chicken", "Grilled chicken with BBQ sauce", 12.99, Arrays.asList("BBQ Sauce", "Chicken", "Red Onions", "Cilantro")),
                    new Pizza("Veggie Supreme", "Loaded with fresh vegetables", 11.99, Arrays.asList("Bell Peppers", "Mushrooms", "Olives", "Onions", "Tomatoes")),
                    new Pizza("Meat Lovers", "For the carnivores", 13.99, Arrays.asList("Pepperoni", "Sausage", "Bacon", "Ham")),
                    new Pizza("Hawaiian", "The controversial sweet/savory mix", 11.49, Arrays.asList("Ham", "Pineapple", "Mozzarella"))
            );

            pizzaRepository.saveAll(pizzas);
            System.out.println(pizzas.size() + " pizzas loaded!");
        }

    }
}
