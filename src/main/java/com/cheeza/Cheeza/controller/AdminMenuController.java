package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.PizzaRequest;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.service.PizzaService;
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
@PreAuthorize("hasRole(ADMIN)")
public class AdminMenuController {

    private final PizzaService pizzaService;

    @Value("${pizza.upload.dir}")
    private String uploadDir;


   public AdminMenuController(  PizzaService pizzaService){
        this.pizzaService = pizzaService;
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
            RedirectAttributes redirectAttributes
    ){
       if(result.hasErrors()|| file.isEmpty()){
           if(file.isEmpty()){
               result.rejectValue("imageFile", "error.pizza", "Image is required");
           }
           return "admin/pizza-form";
       }
       try {
           String fileName = pizzaService.storeImage(file);
           Pizza pizza = pizzaService.createPizza(request, fileName);
           redirectAttributes.addFlashAttribute("success", "Pizza added successfully!");
       }catch (IOException e){
           result.rejectValue("imageFile", "error.pizza", "Image upload failed");
           return "admin/pizza-form";
       }
        return "redirect:/admin/menu";

    }


    /*@PostMapping("/pizzas")
    public String createPizza(
            @RequestParam String name,
            @RequestParam double basePrice,
            @RequestParam String description,
            @RequestParam MultipartFile imageFile,
            RedirectAttributes redirectAttributes) throws IOException {

        String fileName = pizzaService.storeImage(imageFile);
        Pizza pizza = Pizza.builder(name, basePrice)
                .description(description)
                .imageFileName(fileName)
                .build();

        pizzaService.savePizza(pizza);
        redirectAttributes.addFlashAttribute("success", "Pizza added!");
        return "redirect:/admin/menu";
    }*/

    @PostMapping("/pizzas/{pizzaId}/toppings")
    public String addTopping(@PathVariable Long pizzaId,
                             @RequestParam Long toppingId,
                             RedirectAttributes redirectAttributes) {
        pizzaService.addToppingToPizza(pizzaId, toppingId);
        redirectAttributes.addFlashAttribute("success", "Topping added!");
        return "redirect:/admin/menu";
    }
}
