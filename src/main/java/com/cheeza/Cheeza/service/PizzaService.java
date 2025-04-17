package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.model.Pizza;

import java.util.List;
import java.util.Optional;

public interface PizzaService {
    List<Pizza> getAllPizzas();
    Optional<Pizza> getPizzaById(Long id);
    Pizza savePizza(Pizza pizza);
}
