package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private final PizzaRepository pizzaRepository;
    @Autowired
    private final ToppingRepository toppingRepository;

    public DataLoader(PizzaRepository pizzaRepository, ToppingRepository toppingRepository){
        this.pizzaRepository=pizzaRepository;
        this.toppingRepository = toppingRepository;
    }

    @Override
    public void run(String... args)throws Exception{
        //only load data if database is empty
        if(pizzaRepository.count()==0){
//        create and save toppings
            Map<String, Topping> toppings = new HashMap<>();
            String [][] toppingData ={
                    {"Tomato","true","vegetable"},
                    {"Mozzarella","false","dairy"},
                    {"Basil", "true", "herb"},
                    {"Pepperoni","false","meat"},
                    {"BBQ Sauce","true","sauce"},
                    {"Chicken","false","meat"}
                    // more toppings
            };

            for (String[] data: toppingData){
                Topping topping =new Topping();
                topping.setName(data[0]);
                topping.setVegetarian(Boolean.parseBoolean(data[1]));
                topping.setPrice(2.20);
                toppings.put(data[0],toppingRepository.save(topping) );
            }
            // create pizzas with proper toppings
            List<Pizza>pizzas = Arrays.asList(
                    createPizza("Margherita","classic tomato and mozzarella",8.99,toppings.get("Tomato")
                    , toppings.get("Mozzarella"),toppings.get("Basil")),
                    createPizza("Pepperoni","spicy pepperoni with extra cheese",10.99,
                            toppings.get("Tomato"), toppings.get("Mozzarella"), toppings.get("Pepperoni"))
            // more Pizzas
            );

           

            pizzaRepository.saveAll(pizzas);
            System.out.println(pizzas.size() + " pizzas loaded!");
        }

    }

    private Pizza createPizza(String name, String dsec, double price, Topping... toppings) {
        Pizza pizza = new Pizza();
        pizza.setName(name);
        pizza.setDescription(dsec);
        pizza.setBasePrice(price);
        pizza.setAvailableToppings(new HashSet<>(Arrays.asList(toppings)));
        return pizza;
    }
}
