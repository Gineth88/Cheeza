
package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.config.PizzaUploadProperties;
import com.cheeza.Cheeza.dto.PizzaRequest;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.ToppingRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PizzaService {
    private static final Logger log = LoggerFactory.getLogger(PizzaService.class);

    private final PizzaRepository pizzaRepository;
    private final ToppingRepository toppingRepository;
    private final PizzaUploadProperties pizzaUploadProperties;
    private final ToppingService toppingService;

    private static final Map<String, Double> SIZE_MULTIPLIERS = Map.of(
            "S", 1.0,
            "M", 1.2,
            "L", 1.5,
            "XL", 1.8
    );
    private static final Map<String, Double> CRUST_MULTIPLIERS = Map.of(
            "Regular", 1.0,
            "Thin", 1.1,
            "Thick", 1.2,
            "Stuffed", 1.3
    );

    @Autowired
    public PizzaService(PizzaRepository pizzaRepository,
                        ToppingRepository toppingRepository,
                        PizzaUploadProperties pizzaUploadProperties,
                        ToppingService toppingService) {
        this.pizzaRepository = pizzaRepository;
        this.toppingRepository = toppingRepository;
        this.pizzaUploadProperties = pizzaUploadProperties;
        this.toppingService = toppingService;
        createUploadDirectory();
    }

    @PostConstruct
    public void init() {
        log.info("PizzaService initialized with upload directory: {}", pizzaUploadProperties.getUploadDir());
        log.info("Image URL path: {}", pizzaUploadProperties.getImageUrlPath());
    }

    private void createUploadDirectory() {
        try {
            Path path = Paths.get(pizzaUploadProperties.getUploadDir());
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created upload directory at: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public List<Pizza> getAllPizzas() {
        return pizzaRepository.findAll(Sort.by("name"));
    }

    public Pizza getPizzaById(Long id) {
        return pizzaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pizza not found"));
    }

    public Pizza savePizza(Pizza pizza) {
        // Ensure an image is set if it's null
        if (pizza.getImageFileName() == null || pizza.getImageFileName().isEmpty()) {
            pizza.setImageFileName(getDefaultImageName());
            log.info("Setting default image for pizza: {}", pizza.getName());
        }
        return pizzaRepository.save(pizza);
    }

    public String storeImage(MultipartFile file) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            log.warn("Empty file provided for image storage");
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String fileName = UUID.randomUUID() + "_" +
                StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        Path targetPath = Paths.get(pizzaUploadProperties.getUploadDir()).resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Stored image file at: {}", targetPath);
        log.info("Image URL will be: {}{}", pizzaUploadProperties.getImageUrlPath(), fileName);


        return fileName;
    }

    public Pizza createPizza(PizzaRequest request, String imageFileName) {
        log.info("Creating new pizza: {} with image: {}", request.getName(), imageFileName);

        Pizza pizza = Pizza.builder(request.getName(), request.getBasePrice())
                .description(request.getDescription())
                .imageFileName(imageFileName)
                .build();

        return savePizza(pizza);
    }

    public Pizza updatePizza(Long id, PizzaRequest request, MultipartFile file) throws IOException {
        Pizza pizza = getPizzaById(id);
        pizza.setName(request.getName());
        pizza.setDescription(request.getDescription());
        pizza.setBasePrice(request.getBasePrice());

        if (file != null && !file.isEmpty()) {
            String fileName = storeImage(file);
            pizza.setImageFileName(fileName);
            log.info("Updated pizza {} with new image: {}", id, fileName);
        }

        return pizzaRepository.save(pizza);
    }

    public void deletePizza(Long id) {
        pizzaRepository.deleteById(id);
    }

    public Pizza customizePizza(Long pizzaId, Set<Long> toppingIds) {
        Pizza pizza = getPizzaById(pizzaId);
        Set<Topping> toppings = toppingRepository.findAllById(toppingIds)
                .stream()
                .peek(topping -> {
                    if (topping.getAdditionalPrice() == null) {
                        topping.setAdditionalPrice(0.0); // Default value
                    }
                })
                .collect(Collectors.toSet());

        pizza.setSelectedToppings(toppings);
        return pizzaRepository.save(pizza);
    }


    public Pizza addToppingToPizza(Long pizzaId, Long toppingId) {
        Pizza pizza = getPizzaById(pizzaId);
        Topping topping = toppingService.getToppingById(toppingId);
        pizza.getAvailableToppings().add(topping);
        topping.getPizzas().add(pizza);
        return pizzaRepository.save(pizza);
    }


    public Pizza removeToppingFromPizza(Long pizzaId, Long toppingId) {
        Pizza pizza = getPizzaById(pizzaId);
        Topping topping = toppingService.getToppingById(toppingId);
        pizza.getAvailableToppings().remove(topping);
        topping.getPizzas().remove(pizza);
        return pizzaRepository.save(pizza);
    }


    private String getDefaultImageName() {
        return "default-pizza.jpg";
    }


    public double calculateTotalPrice(Pizza pizza, String size, String crustType) {
        double basePrice = pizza.getBasePrice();
        double sizeMultiplier = SIZE_MULTIPLIERS.getOrDefault(size, 1.0);
        double crustMultiplier = CRUST_MULTIPLIERS.getOrDefault(crustType, 1.0);

        // Calculate the total price of toppings by summing their individual prices
        double toppingsPrice = 0.0;
        if (pizza.getSelectedToppings() != null) {
            toppingsPrice = pizza.getSelectedToppings().stream()
                    .mapToDouble(topping -> topping.getAdditionalPrice() != null ? topping.getAdditionalPrice() : 0.0)
                    .sum();
        }

        log.debug("Price calculation: Base={}, Size Multiplier={}, Crust Multiplier={}, Toppings={}",
                basePrice, sizeMultiplier, crustMultiplier, toppingsPrice);

        return (basePrice * sizeMultiplier * crustMultiplier) + toppingsPrice;
    }
}
