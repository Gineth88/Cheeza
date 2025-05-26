package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.PizzaRequest;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.service.PizzaService;
import com.cheeza.Cheeza.service.ToppingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin/menu")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuController {
    private final PizzaService pizzaService;
    private final ToppingService toppingService;

    public AdminMenuController(PizzaService pizzaService, ToppingService toppingService) {
        this.pizzaService = pizzaService;
        this.toppingService = toppingService;
    }

    @GetMapping
    public String menuDashboard(Model model) {
        model.addAttribute("pizzas", pizzaService.getAllPizzas());
        return "admin/menu-dashboard";
    }

    @GetMapping("/pizzas/new")
    public String showPizzaForm(Model model) {
        model.addAttribute("pizzaRequest", new PizzaRequest());
        return "admin/pizza-form";
    }

    @PostMapping("/pizzas")
    public String createPizza(
            @Valid @ModelAttribute PizzaRequest request,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        // Validate file
        if (file == null || file.isEmpty()) {
            result.rejectValue("imageFile", "error.imageFile", "Please select an image");
            return "admin/pizza-form";
        }

        try {
            String fileName = pizzaService.storeImage(file);
            Pizza pizza = pizzaService.createPizza(request, fileName);
            redirectAttributes.addFlashAttribute("success", "Pizza added successfully!");
        } catch (IOException e) {
            result.rejectValue("imageFile", "error.imageFile", "Failed to save image");
            return "admin/pizza-form";
        } catch (IllegalArgumentException e) {
            result.rejectValue("imageFile", "error.imageFile", e.getMessage());
            return "admin/pizza-form";
        }

        return "redirect:/admin/menu";
    }

    @PostMapping("/pizzas/{pizzaId}/toppings")
    public String addTopping(@PathVariable Long pizzaId,
                             @RequestParam Long toppingId,
                             RedirectAttributes redirectAttributes) {
        pizzaService.addToppingToPizza(pizzaId, toppingId);
        redirectAttributes.addFlashAttribute("success", "Topping added!");
        return "redirect:/admin/menu";
    }

    @GetMapping("/pizzas/edit/{id}")
    public String editPizzaForm(@PathVariable Long id, Model model) {
        Pizza pizza = pizzaService.getPizzaById(id);
        model.addAttribute("pizzaRequest", pizzaToRequest(pizza));
        return "admin/pizza-form";
    }

    @PostMapping("/pizzas/update/{id}")
    public String updatePizza(
            @PathVariable Long id,
            @Valid @ModelAttribute PizzaRequest request,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        if (result.hasErrors()) {
            return "admin/pizza-form";
        }

        Pizza pizza = pizzaService.updatePizza(id, request, file);
        redirectAttributes.addFlashAttribute("success", "Pizza updated successfully!");
        return "redirect:/admin/menu";
    }

    @PostMapping("/pizzas/delete/{id}")
    public String deletePizza(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pizzaService.deletePizza(id);
        redirectAttributes.addFlashAttribute("success", "Pizza deleted successfully!");
        return "redirect:/admin/menu";
    }

    private PizzaRequest pizzaToRequest(Pizza pizza) {
        return PizzaRequest.builder()
                .id(pizza.getId())
                .name(pizza.getName())
                .description(pizza.getDescription())
                .basePrice(pizza.getBasePrice())
                .imageFileName(pizza.getImageFileName())
                .imageFile(pizza.getImageFile())
                // Lists will get default values from @Builder.Default
                .build();
    }
    @GetMapping("/toppings")
    public String manageToppings(Model model) {
        model.addAttribute("toppings", toppingService.getAllToppings());
        return "admin/topping-list";
    }

    @GetMapping("/toppings/new")
    public String showToppingForm(Model model) {
        model.addAttribute("topping", new Topping());
        return "admin/topping-form";
    }

    @PostMapping("/toppings")
    public String createTopping(
            @Valid @ModelAttribute("topping") Topping topping,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/topping-form";
        }

        toppingService.saveTopping(topping);
        redirectAttributes.addFlashAttribute("success", "Topping created successfully!");
        return "redirect:/admin/menu/toppings";
    }

    @GetMapping("/toppings/edit/{id}")
    public String editToppingForm(@PathVariable Long id, Model model) {
        Topping topping = toppingService.getToppingById(id);
        model.addAttribute("topping", topping);
        return "admin/topping-form";
    }

    @PostMapping("/toppings/update/{id}")
    public String updateTopping(
            @PathVariable Long id,
            @Valid @ModelAttribute("topping") Topping topping,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/topping-form";
        }

        topping.setId(id);
        toppingService.saveTopping(topping);
        redirectAttributes.addFlashAttribute("success", "Topping updated successfully!");
        return "redirect:/admin/menu/toppings";
    }

    @PostMapping("/toppings/delete/{id}")
    public String deleteTopping(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        toppingService.deleteTopping(id);
        redirectAttributes.addFlashAttribute("success", "Topping deleted successfully!");
        return "redirect:/admin/menu/toppings";
    }
}
