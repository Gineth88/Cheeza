package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.Pizza;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PizzaRepository extends JpaRepository<Pizza, Long> {
    List<Pizza> findByAvailableTrue();
    List<Pizza> findByNameContainingIgnoreCase(String name);
    List<Pizza> findByFeaturedTrue();

    @EntityGraph(attributePaths = "availableToppings")
    List<Pizza>findAll(Sort sort);
}
