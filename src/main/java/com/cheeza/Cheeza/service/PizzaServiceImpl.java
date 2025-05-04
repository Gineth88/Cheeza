package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.repository.PizzaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PizzaServiceImpl implements PizzaService{
    private final PizzaRepository pizzaRepository;

    public PizzaServiceImpl(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    @Override
    public List<Pizza> getAllPizzas() {
        return pizzaRepository.findAll();
    }

    @Override
    public Optional<Pizza> getPizzaById(Long id) {
        return pizzaRepository.findById(id);
    }

    @Override
    public Pizza savePizza(Pizza pizza) {
        return pizzaRepository.save(pizza);
    }
}
