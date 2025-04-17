package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.CartItem;
import com.cheeza.Cheeza.model.Pizza;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {
    private Map<Long,CartItem> cart = new ConcurrentHashMap<>();
    private final PizzaService pizzaService;

    @Autowired
    public CartService(PizzaService pizzaService) {
        this.pizzaService = pizzaService;
    }


    public void addPizza(Pizza pizza, int quantity){
        cart.compute(pizza.getId(),(id,item) ->

                    item == null ?
                            new CartItem(pizza,quantity):
                            item.addQuantity(quantity)

                );

    }

    public void addToCart(Long pizzaId, int quantity) {
        Pizza pizza = pizzaService.getPizzaById(pizzaId)
                .orElseThrow(() -> new RuntimeException("Pizza not found"));
        cart.merge(pizza.getId(),
                new CartItem(pizza, quantity),
                (existing, newItem) -> new CartItem(pizza, existing.getQuantity() + quantity)
        );
    }

    public List<CartItem> getCartItems(){
        return new ArrayList<>(cart.values());
    }
    public double getTotal(){
        return cart.values().stream()
                .mapToDouble(item -> item.getPizza().getBasePrice()*item.getQuantity())
                .sum();
    }

    public void clearCart(){
        cart.clear();
    }
}
