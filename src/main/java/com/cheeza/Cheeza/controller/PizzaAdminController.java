package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.model.Pizza;

import com.cheeza.Cheeza.service.PizzaService;
import com.cheeza.Cheeza.service.PizzaServiceImpl;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/admin/pizzas")
public class PizzaAdminController {

    private final PizzaServiceImpl pizzaService;

    @Value("${pizza.upload.dir}")
    private String uploadDir;

    public PizzaAdminController(PizzaServiceImpl pizzaService){
        this.pizzaService = pizzaService;
    }

    @GetMapping("/new")
    public String showAddPizzaForm(Model model){
        model.addAttribute("pizza", new Pizza());
        return "/admin/add-pizza";
    }
    @PostMapping
    public String addPizza(@Valid @ModelAttribute("pizza") Pizza pizza,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        MultipartFile file = pizza.getImageFile();
        if (file != null && !file.isEmpty()) {
            if (!file.getContentType().startsWith("image/")) {
                result.rejectValue("imageFile", "error.pizza", "Only image files are allowed");
            }

            try {
                String fileName = storeFile(file);
                pizza.setImageFileName(fileName);
            } catch (IOException e) {
                result.rejectValue("imageFile", "error.pizza", "Failed to upload image");
            }
        }

        if (result.hasErrors()) {
            return "admin/add-pizza";
        }

        pizzaService.savePizza(pizza);
        redirectAttributes.addFlashAttribute("success", "Pizza added successfully!");
        return "redirect:/menu";
    }
    private String storeFile(MultipartFile file) throws IOException, IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + originalFileName;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }
}
