
package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.CartItem;
import com.cheeza.Cheeza.dto.CustomPizzaRequest;
import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.OrderRepository;
import com.cheeza.Cheeza.service.CartService;
import com.cheeza.Cheeza.service.NotificationService;
import com.cheeza.Cheeza.service.PizzaService;
import com.cheeza.Cheeza.service.ToppingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final PizzaService pizzaService;
    private final ToppingService toppingService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;

    @ModelAttribute("cartCount")
    public int getCartCountForHeader(HttpSession session) {
        return cartService.getCartSize(session);
    }

    public CartController(CartService cartService, PizzaService pizzaService, ToppingService toppingService, NotificationService notificationService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.pizzaService = pizzaService;
        this.toppingService = toppingService;
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/add/{pizzaId}")
    public String addToCart(@PathVariable Long pizzaId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            List<CartItem> items = cartService.getCartItems(session);
            log.debug("CHECKOUT: Session {} cart contains {} items", session.getId(), items.size());
            log.debug("Adding pizza with ID {} to cart, quantity: {}", pizzaId, quantity);
            Pizza pizza = pizzaService.getPizzaById(pizzaId);
            CartItem cartItem = new CartItem(pizza, quantity);
            cartService.addToCart(cartItem, session);
            redirectAttributes.addFlashAttribute("success", "Pizza added to cart!");
            return "redirect:/menu";
        } catch (Exception e) {
            log.error("Error adding pizza to cart", e);
            redirectAttributes.addFlashAttribute("error", "Failed to add pizza to cart: " + e.getMessage());
            return "redirect:/menu";
        }
    }

    @PostMapping("/add-custom")
    public String addCustomToCart(@Valid @ModelAttribute CustomPizzaRequest request,
                                  BindingResult bindingResult,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.customPizzaRequest", bindingResult);
            redirectAttributes.addFlashAttribute("customPizzaRequest", request);
            return "redirect:/menu/pizza/" + request.getPizzaId() + "/customize";
        }

        try {
            log.debug("Adding custom pizza to cart: {}", request);
            Pizza pizza = pizzaService.getPizzaById(request.getPizzaId());

            Pizza customizedPizza = new Pizza();
            customizedPizza.setId(pizza.getId());
            customizedPizza.setName(pizza.getName() + " (Custom)");
            customizedPizza.setDescription(pizza.getDescription());
            customizedPizza.setBasePrice(pizza.getBasePrice());
            customizedPizza.setImageFileName(pizza.getImageFileName());
            customizedPizza.setSize(request.getSize());
            customizedPizza.setCrustType(request.getCrustType());
            customizedPizza.setSauceType(request.getSauceType());

            // Handle potential null toppingIds
            Set<Topping> toppings = new HashSet<>();
            if (request.getSelectedToppingIds() != null && !request.getSelectedToppingIds().isEmpty()) {
                toppings = toppingService.getToppingsByIds(request.getSelectedToppingIds());
                customizedPizza.setSelectedToppings(toppings);

                // Log toppings for debugging
                log.debug("Selected toppings: {}", toppings.stream()
                        .map(t -> t.getName() + "(" + t.getAdditionalPrice() + ")")
                        .collect(Collectors.joining(", ")));
            }

            // Calculate the final price including toppings
            double totalPrice = pizzaService.calculateTotalPrice(customizedPizza,
                    customizedPizza.getSize(), customizedPizza.getCrustType());

            // Set a calculated price that will be used in the cart
            customizedPizza.setCalculatedPrice(totalPrice);
            log.debug("Calculated total price for custom pizza: {}", totalPrice);

            CartItem cartItem = new CartItem(customizedPizza, request.getQuantity());
            cartService.addToCart(cartItem, session);
            redirectAttributes.addFlashAttribute("success", "Custom pizza added to cart!");
        } catch (Exception e) {
            log.error("Failed to add custom pizza to cart", e);
            redirectAttributes.addFlashAttribute("error", "Failed to add pizza to cart: " + e.getMessage());
            return "redirect:/menu/pizza/" + request.getPizzaId() + "/customize";
        }
        return "redirect:/cart";
    }


    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        try {
            log.debug("Viewing cart with session ID: {}", session.getId());
            List<CartItem> items = cartService.getCartItems(session);
            log.debug("Cart contains {} items", items.size());

            model.addAttribute("cartItems", items);
            model.addAttribute("totalPrice", cartService.getTotal(session));
            return "cart";
        } catch (Exception e) {
            log.error("Error viewing cart", e);
            // Reset the cart if there's an error
            session.removeAttribute("CART_ITEMS");
            model.addAttribute("error", "There was a problem loading your cart. We've reset it to fix the issue.");
            model.addAttribute("cartItems", Collections.emptyList());
            model.addAttribute("totalPrice", 0.0);
            return "cart";
        }
    }

    @PostMapping("/remove/{index}")
    public String removeFromCartByIndex(@PathVariable int index,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(index, session);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (Exception e) {
            log.error("Error removing item from cart", e);
            redirectAttributes.addFlashAttribute("error", "Failed to remove item from cart");
        }
        return "redirect:/cart";
    }


    @GetMapping("/count")
    @ResponseBody
    public Map<String, Integer> getCartCount(HttpSession session) {
        try {
            int count = cartService.getCartSize(session);
            return Collections.singletonMap("count", count);
        } catch (Exception e) {
            log.error("Error getting cart count", e);
            return Collections.singletonMap("count", 0);
        }
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        List<CartItem> items = cartService.getCartItems(session);
        if(items == null) items = Collections.emptyList(); // Defensive fallback
        model.addAttribute("cartItems", items);
        model.addAttribute("totalPrice", cartService.getTotal(session));
        log.debug("CHECKOUT: Session {} cart contains {} items", session.getId(), items.size());
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("totalPrice", cartService.getTotal(session));
        model.addAttribute("paymentMethods", Arrays.asList("CreditCard", "Cash", "PixieWallet"));
        // Add a default paymentMethod
        model.addAttribute("defaultPaymentMethod", "Cash");
        return "checkout";
    }

    @GetMapping("/points")
    @ResponseBody
    public int getCurrentUserPoints(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return user.getLoyaltyPoints();
            } else {
                log.warn("User not found in session.");
                return 0; // Or handle the case where the user is not in the session
            }
        } catch (Exception e) {
            log.error("Error getting user points", e);
            return 0;
        }
    }


    @PostMapping("/api/add/{pizzaId}")
    @ResponseBody
    public Map<String, Object> ajaxAddToCart(@PathVariable Long pizzaId,
                                             @RequestParam(defaultValue = "1") int quantity,
                                             HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Pizza pizza = pizzaService.getPizzaById(pizzaId);
            CartItem cartItem = new CartItem(pizza, quantity);
            cartService.addToCart(cartItem, session);
            List<CartItem> cartItems = cartService.getCartItems(session);

            // Find updated item
            CartItem updated = cartItems.stream()
                    .filter(ci -> ci.getPizzaId().equals(pizzaId))
                    .findFirst().orElse(null);

            double totalPrice = cartService.getTotal(session);

            resp.put("success", true);
            resp.put("item", Map.of(
                    "quantity", updated != null ? updated.getQuantity() : 0,
                    "totalPrice", updated != null ? updated.getTotalPrice() : 0.0
            ));
            resp.put("totalPrice", totalPrice);

            if (updated == null || updated.getQuantity() == 0) {
                resp.put("removed", true);
            }
            resp.put("successMsg", "Cart updated!");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("errorMsg", "Unable to update cart: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping("/api/remove/{index}")
    @ResponseBody
    public Map<String, Object> ajaxRemoveFromCart(@PathVariable int index, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            cartService.removeFromCart(index, session);
            double totalPrice = cartService.getTotal(session);
            resp.put("success", true);
            resp.put("totalPrice", totalPrice);
            resp.put("successMsg", "Item removed from cart.");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("errorMsg", "Could not remove item: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam(required = false, defaultValue = "Cash") String paymentMethod,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String expiry,
            @RequestParam(required = false) String cvv,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        List<CartItem> cartItems = cartService.getCartItems(session);
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cannot process payment for an empty cart.");
            return "redirect:/cart";
        }

        try {
            // Calculate total (pizza + toppings)
            double total = cartService.getTotal(session);

            // Notification (for coursework, simulate)
            User user = (User) session.getAttribute("user");
            Order order = new Order();
            order.setUser(user);
            // Set other order properties...
            Order savedOrder = orderRepository.save(order);
            if (user != null) {
                String message = "Order placed successfully! Total: LKR " + String.format("%.2f", total);
                com.cheeza.Cheeza.model.Notification notif = new com.cheeza.Cheeza.model.Notification(
                        "Payment Success", message, java.time.LocalDateTime.now()
                );
                notificationService.sendNotification(
                        user,
                        "Order Confirmed",
                        "Your order #" + savedOrder.getId() + " has been placed successfully!"
                );
                notif.setUser(user); // Always set the user!
                user.addNotification(notif);   // This will show in their profile later
            }

            // "Save" the order to database if you want, or just keep in session for coursework
            // Optionally create an Order object/model

            // Clear the cart after order
            cartService.clearCart(session);

            // For coursework, store invoice details in session to show in next invoice page
            session.setAttribute("invoice_cartItems", cartItems);
            session.setAttribute("invoice_total", total);
            session.setAttribute("invoice_cardName", cardName);
            session.setAttribute("invoice_cardNumber", cardNumber);
            session.setAttribute("invoice_paymentMethod", paymentMethod);
            session.setAttribute("invoice_date", java.time.LocalDateTime.now());

            // Show success on invoice page
            redirectAttributes.addFlashAttribute("success", "Order placed successfully! See your invoice below.");
            return "redirect:/cart/invoice";
        } catch (Exception e) {
            log.error("Error during checkout", e);
            redirectAttributes.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/invoice")
    public String showInvoicePage(HttpSession session, Model model, @ModelAttribute("success") String successMsg) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("invoice_cartItems");
        Double total = (Double) session.getAttribute("invoice_total");
        String cardName = (String) session.getAttribute("invoice_cardName");
        String cardNumber = (String) session.getAttribute("invoice_cardNumber");
        String paymentMethod = (String) session.getAttribute("invoice_paymentMethod");
        java.time.LocalDateTime invoiceDate = (java.time.LocalDateTime) session.getAttribute("invoice_date");

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", total);
        model.addAttribute("cardName", cardName);
        model.addAttribute("maskedCardNumber", maskCardNumber(cardNumber)); // Helper method to mask
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("invoiceDate", invoiceDate);
        model.addAttribute("success", successMsg);

        return "invoice";
    }

    // Helper method
    private String maskCardNumber(String num) {
        if (num == null || num.length() < 4) return "****";
        return "**** **** **** " + num.substring(num.length()-4);
    }
}
