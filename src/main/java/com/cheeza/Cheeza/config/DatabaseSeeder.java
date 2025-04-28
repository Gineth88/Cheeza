package com.cheeza.Cheeza.config;

import java.util.List;
import java.util.Set;

import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.ToppingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cheeza.Cheeza.model.Role;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.UserRepository;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                // Admin user
                User admin = new User.Builder(
                        "admin@cheeza.com",  // Required email
                        passwordEncoder.encode("admin123")  // Required password
                )
                        .fullName("Admin User")
                        .role(Role.ADMIN)
                        .build();

                // Regular users
                List<User> users = List.of(
                        new User.Builder(
                                "john@example.com",
                                passwordEncoder.encode("password123")
                        )
                                .fullName("John Doe")
                                .role(Role.CUSTOMER)
                                .build(),

                        new User.Builder(
                                "jane@example.com",
                                passwordEncoder.encode("password123")
                        )
                                .fullName("Jane Smith")
                                .role(Role.CUSTOMER)
                                .build()
                );

                userRepository.saveAll(users);
                userRepository.save(admin);
            }
        };
    }

    @Configuration
    public class DataInitializer {

        @Bean
        CommandLineRunner initDatabase(PizzaRepository pizzaRepo, ToppingRepository toppingRepo) {
            return args -> {
                // Clear existing data
                pizzaRepo.deleteAll();
                toppingRepo.deleteAll();

                // Create toppings
                Topping cheese = toppingRepo.save(new Topping(7L,"Cheese", 1.50, true, "fa-cheese"));
                Topping pepperoni = toppingRepo.save(new Topping(10L,"Pepperoni", 2.00, false, "fa-pepper-hot"));


                // Create pizzas with toppings
                Pizza margherita = new Pizza("Margherita", "Classic tomato and cheese", 9.99, );
                margherita.getAvailableToppings().add(cheese);
                pizzaRepo.save(margherita);

                Pizza pepperoniPizza = new Pizza("Pepperoni", "Spicy pepperoni delight", 12.99, true);
                pepperoniPizza.getAvailableToppings().addAll(Set.of(cheese, pepperoni));
                pizzaRepo.save(pepperoniPizza);
            };
        }
    }
}