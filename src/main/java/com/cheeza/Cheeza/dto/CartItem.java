package com.cheeza.Cheeza.dto;

import com.cheeza.Cheeza.model.Pizza;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItem {
    private Pizza pizza;
    private int quantity;

    public CartItem addQuantity(int additional) {
        return new CartItem(pizza, quantity + additional);
    }
}
