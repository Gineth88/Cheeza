
package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CartService {
    private static final String CART_SESSION_KEY = "CART_ITEMS";
    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private final PizzaService pizzaService;

    @Autowired
    public CartService(PizzaService pizzaService) {
        this.pizzaService = pizzaService;
    }

    public void addToCart(CartItem newItem, HttpSession session) {
        log.debug("Adding item to cart: {}", newItem);
        if (newItem == null) {
            log.error("Cannot add null item to cart");
            return;
        }

        List<CartItem> cartItems = getCartItems(session);

        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.matches(newItem))
                .findFirst();

        if (existingItem.isPresent()) {
            log.debug("Found matching item in cart, updating quantity");
            existingItem.get().addQuantity(newItem.getQuantity());
        } else {
            log.debug("Adding new item to cart");
            cartItems.add(newItem);
        }

        session.setAttribute(CART_SESSION_KEY, cartItems);
        log.debug("Cart now contains {} items", cartItems.size());
    }

    public List<CartItem> getCartItems(HttpSession session) {
        if (session == null) {
            log.error("Session is null when getting cart items");
            return new ArrayList<>();
        }

        try {
            @SuppressWarnings("unchecked")
            List<CartItem> items = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
            return items != null ? new ArrayList<>(items) : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error retrieving cart items from session", e);
            // Reset the cart if there's an error
            session.removeAttribute(CART_SESSION_KEY);
            return new ArrayList<>();
        }
    }

    public double getTotal(HttpSession session) {
        return getCartItems(session).stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    public void clearCart(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CART_SESSION_KEY);
        }
    }

    public void removeFromCart(int index, HttpSession session) {
        List<CartItem> cartItems = getCartItems(session);
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
            session.setAttribute(CART_SESSION_KEY, cartItems);
        }
    }

    public int getCartSize(HttpSession session) {
        return getCartItems(session).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
