package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ToppingService {
    private final ToppingRepository toppingRepository;

    public List<Topping> getAllToppings() {
        return toppingRepository.findAll(Sort.by("name"));
    }


    public Set<Topping> getToppingsByIds(List<Long> toppingIds) {
        if (toppingIds == null || toppingIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(toppingRepository.findAllById(toppingIds));
    }


    public Topping getToppingById(Long id) {
        return toppingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topping not found"));
    }


    public Topping saveTopping(Topping topping) {
        return toppingRepository.save(topping);
    }


    public void deleteTopping(Long id) {
        toppingRepository.deleteById(id);
    }
}
