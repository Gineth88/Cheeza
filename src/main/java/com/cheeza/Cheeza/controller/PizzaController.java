package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.repository.PizzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas")
public class PizzaController {
    @Autowired
    private final PizzaRepository pizzaRepository;

    public PizzaController(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    @GetMapping("/")
    public List<Pizza>getAllPizzas(){
        return pizzaRepository.findAll();
    }
    @GetMapping("/available")
    public List<Pizza>getAvailablePizzas(){
        return pizzaRepository.findByAvailableTrue();
    }

    @GetMapping("/test")
    public String testEndpoint() {
        long count = pizzaRepository.count(); // Inherited from JpaRepository
        return "Total pizzas in DB: " + count;
    }

}
