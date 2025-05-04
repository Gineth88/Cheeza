package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.Topping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping,Long> {
//    List<Topping> findVegetarian();\
Topping findByName(String name);
}
