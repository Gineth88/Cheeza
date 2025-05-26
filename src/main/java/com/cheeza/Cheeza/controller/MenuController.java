package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.CartItem;
import com.cheeza.Cheeza.dto.OrderRequest;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.service.CartService;
import com.cheeza.Cheeza.service.OrderService;
import com.cheeza.Cheeza.service.PizzaService;
import com.cheeza.Cheeza.service.ToppingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class MenuController {
    private final PizzaService pizzaService;
    private final OrderService orderService;
    private final CartService cartService;
    private final ToppingService toppingService;

    @Autowired
    public MenuController(PizzaService pizzaService, OrderService orderService,
                          CartService cartService, ToppingService toppingService) {
        this.pizzaService = pizzaService;
        this.orderService = orderService;
        this.cartService = cartService;
        this.toppingService = toppingService;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("featuredPizzas",
                "sasuge pizza, devilled chicken pizza, sweet corn Pizza" +
                        "All in at LKR1800 Order Now today only");
        return "home";
    }

    @GetMapping("/menu")
    public String showMenu(Model model) {
        model.addAttribute("pizzas", pizzaService.getAllPizzas());
        return "menu";
    }

    @PostMapping("/order/submit")
    public String submitOrder(@Valid @ModelAttribute OrderRequest orderRequest,
                              BindingResult bindingResult,
                              HttpSession session) {  // Add HttpSession parameter
        if (bindingResult.hasErrors()) {
            return "order-form";
        }
        orderService.createOrderFromCart(orderRequest, session);  // Pass session
        return "redirect:/order/success";
    }

    // Pizza Details and Customization Endpoints
    @GetMapping("/menu/pizza/{id}")
    public String viewPizzaDetails(@PathVariable Long id, Model model) {
        Pizza pizza = pizzaService.getPizzaById(id);
        List<Topping> allToppings = toppingService.getAllToppings();

        model.addAttribute("pizza", pizza);
        model.addAttribute("allToppings", allToppings);
        model.addAttribute("availableSizes", Map.of(
                "S", 0.8,
                "M", 1.0,
                "L", 1.2,
                "XL", 1.5
        ));
        model.addAttribute("crustTypes", List.of("Regular", "Thin", "Thick", "Stuffed"));
        model.addAttribute("sauceTypes", List.of("Tomato", "BBQ", "Alfredo", "Pesto"));

        return "pizza-details";
    }

    @GetMapping("/menu/pizza/{id}/customize")
    public String showCustomizeForm(@PathVariable Long id, Model model) {
        Pizza pizza = pizzaService.getPizzaById(id);
        List<Topping> allToppings = toppingService.getAllToppings();

        model.addAttribute("pizza", pizza);
        model.addAttribute("allToppings", allToppings);
        model.addAttribute("availableSizes", Map.of(
                "S", 0.8,
                "M", 1.0,
                "L", 1.2,
                "XL", 1.5
        ));
        model.addAttribute("crustTypes", List.of("Regular", "Thin", "Thick", "Stuffed"));
        model.addAttribute("sauceTypes", List.of("Tomato", "BBQ", "Alfredo", "Pesto"));

        return "customize-pizza";
    }

    @PostMapping("/menu/pizza/{id}/customize")
    public String processCustomization(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> selectedToppingIds,
            @RequestParam String size,
            @RequestParam String crustType,
            @RequestParam String sauceType,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Pizza pizza = pizzaService.getPizzaById(id);

        // Create a copy of the pizza with customizations
        Pizza customizedPizza = new Pizza();
        customizedPizza.setId(pizza.getId());
        customizedPizza.setName(pizza.getName());
        customizedPizza.setDescription(pizza.getDescription());
        customizedPizza.setBasePrice(pizza.getBasePrice());
        customizedPizza.setImageFileName(pizza.getImageFileName());

        // Apply customizations
        customizedPizza.setSize(size);
        customizedPizza.setCrustType(crustType);
        customizedPizza.setSauceType(sauceType);

        // Set selected toppings
        Set<Topping> selectedToppings = new HashSet<>();
        if (selectedToppingIds != null) {
            selectedToppingIds.forEach(toppingId -> {
                Topping topping = toppingService.getToppingById(toppingId);
                selectedToppings.add(topping);
            });
        }
        customizedPizza.setSelectedToppings(selectedToppings);

        // Set size multiplier
        double sizeMultiplier = switch (size) {
            case "S" -> 0.8;
            case "L" -> 1.2;
            case "XL" -> 1.5;
            default -> 1.0; // M
        };
        customizedPizza.setSizeMultiplier(sizeMultiplier);

        // Add to cart
        CartItem cartItem = new CartItem(customizedPizza, 1);
        List<CartItem> cart = (List<CartItem>) session.getAttribute("CART_ITEMS");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("CART_ITEMS", cart);
        }
        cart.add(cartItem);

        redirectAttributes.addFlashAttribute("success", "Pizza added to cart!");
        return "redirect:/cart";
    }

    // Helper endpoint to calculate price (for AJAX calls)
    @GetMapping("/menu/pizza/calculate-price")
    @ResponseBody
    public Map<String, Double> calculatePrice(
            @RequestParam double basePrice,
            @RequestParam(required = false) List<Long> toppingIds,
            @RequestParam String size) {

        double toppingsPrice = 0;
        if (toppingIds != null) {
            toppingsPrice = toppingIds.stream()
                    .mapToDouble(id -> toppingService.getToppingById(id).getAdditionalPrice())
                    .sum();
        }

        double sizeMultiplier = switch (size) {
            case "S" -> 0.8;
            case "L" -> 1.2;
            case "XL" -> 1.5;
            default -> 1.0; // M
        };

        double totalPrice = (basePrice + toppingsPrice) * sizeMultiplier;

        return Map.of("price", totalPrice);
    }
}
