package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.PizzaRequest;
import com.cheeza.Cheeza.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/menu")
@PreAuthorize("hasRole(ADMIN)")
public class AdminMenuController {
    private final MenuService menuService;

    @Autowired
   public AdminMenuController(MenuService menuService){
        this.menuService=menuService;
    }

    // Show creative management dashboard
    @GetMapping
    public String menuDashboard(Model model) {
        model.addAttribute("pizzas", menuService.getAllPizzas());
        model.addAttribute("toppings", menuService.getAllToppings());
        return "admin/menu-dashboard";
    }

    // Pizza creation form
    @GetMapping("/pizzas/new")
    public String showPizzaForm(Model model) {
        model.addAttribute("pizza", new PizzaRequest());
        model.addAttribute("allToppings", menuService.getAllToppings());
        return "admin/pizza-form";
    }

    @PostMapping("/pizzas")
    public String createPizza(@Valid @ModelAttribute PizzaRequest request,
                              BindingResult result) {
        if (result.hasErrors()) return "admin/pizza-form";
        menuService.createPizza(request);
        return "redirect:/admin/menu?pizza_added";
    }
}
