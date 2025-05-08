package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.PizzaRequest;
import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PizzaService {
    private final PizzaRepository pizzaRepository;
    private final ToppingRepository toppingRepository;
    private final String uploadDir = "uploads/";

    public List<Pizza>getAllPizzas(){
        return pizzaRepository.findAll(Sort.by("name"));
    }
    public Pizza getPizzaById(Long id) {
        return pizzaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pizza not found"));
    }

    public Pizza savePizza(Pizza pizza) {
        return pizzaRepository.save(pizza);
    }

//    public List<Topping>getAllToppings(){
//        return toppingRepository.findAll(Sort.by("name"));
//    }

    public Pizza addToppingToPizza(Long pizzaId, Long toppingId){
        Pizza pizza = getPizzaById(pizzaId);
        Topping topping = toppingRepository.findById(toppingId)
                .orElseThrow(()-> new RuntimeException("Topping not found"));
        pizza.getAvailableToppings().add(topping);
        return pizzaRepository.save(pizza);

    }

    public String storeImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
        return fileName;

    }

    // In PizzaService.java
    public Pizza createPizza(PizzaRequest request, String imageFileName) {
        Pizza pizza = Pizza.builder(request.getName(), request.getBasePrice())
                .description(request.getDescription())
                .imageFileName(imageFileName)
                .build();

        return pizzaRepository.save(pizza);
    }

}
