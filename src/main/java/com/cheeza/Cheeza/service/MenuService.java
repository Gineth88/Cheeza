package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.PizzaRequest;
import com.cheeza.Cheeza.dto.PizzaResponse;
import com.cheeza.Cheeza.dto.ToppingResponse;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final PizzaRepository pizzaRepository;
    private final ToppingRepository toppingRepository;
    private final ModelMapper modelMapper;


    public Pizza createPizza(PizzaRequest request) {
        Set<Topping> toppings = request.getToppingIds().stream()
                .map(toppingId -> toppingRepository.findById(toppingId)
                        .orElseThrow(() -> new RuntimeException("Topping not found with id: " + toppingId)))
                .collect(Collectors.toSet());

        Pizza pizza = Pizza.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .imageUrl(request.getImageUrl())
                .featured(request.isFeatured())
                .availableToppings(toppings)
                .build();

        return pizzaRepository.save(pizza);
    }
    public Pizza addTopping(Long pizzaId, Long toppingId) {
        Pizza pizza = pizzaRepository.findById(pizzaId).orElseThrow();
        Topping topping = toppingRepository.findById(toppingId).orElseThrow();
        pizza.getAvailableToppings().add(topping);
        return pizzaRepository.save(pizza);
    }
    // Get all pizzas sorted by featured status then name
    public List<PizzaResponse> getAllPizzas() {
        return pizzaRepository.findAll(Sort.by(Sort.Direction.DESC, "featured")
                        .and(Sort.by("name")))
                .stream()
                .map(this::convertToPizzaResponse)
                .toList();
    }

    // Get all toppings sorted by vegetarian status then name
    public List<ToppingResponse> getAllToppings() {
        return toppingRepository.findAll(Sort.by(Sort.Direction.DESC, "vegetarian")
                        .and(Sort.by("name")))
                .stream()
                .map(this::convertToToppingResponse)
                .toList();
    }

    // Conversion methods
    private PizzaResponse convertToPizzaResponse(Pizza pizza) {
        PizzaResponse response = modelMapper.map(pizza, PizzaResponse.class);
        response.setFormattedPrice(formatPrice(pizza.getBasePrice()));
        return response;
    }

    private ToppingResponse convertToToppingResponse(Topping topping) {
        ToppingResponse response = modelMapper.map(topping, ToppingResponse.class);
        response.setFormattedPrice(formatPrice(topping.getPrice()));
        return response;
    }

    private String formatPrice(double price) {
        return String.format("$%.2f", price);
    }
}

