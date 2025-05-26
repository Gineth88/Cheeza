package com.cheeza.Cheeza.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.cheeza.Cheeza.model.Pizza;
import com.cheeza.Cheeza.model.Topping;
import com.cheeza.Cheeza.repository.PizzaRepository;
import com.cheeza.Cheeza.repository.ToppingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cheeza.Cheeza.model.Role;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final PizzaUploadProperties pizzaUploadProperties;
    private final PizzaRepository pizzaRepository;
    private final ToppingRepository toppingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        logger.info("Database seeder initializing...");
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (userRepository.count() == 0) {
                // Admin user
                User admin = new User.Builder(
                        "admin@cheeza.com",  // Email as identifier
                        passwordEncoder.encode("admin123")
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
                                .build(),
                        new User.Builder(
                                "jane@example.com",
                                passwordEncoder.encode("password123")
                        )
                                .fullName("Jane Smith")
                                .build()
                );

                userRepository.saveAll(users);
                userRepository.save(admin);
                logger.info("Initialized users: {}", userRepository.count());
            } else {
                logger.info("Users already initialized, skipping user initialization.");
            }
        };
    }

    @Bean
    CommandLineRunner initMenu() {
        return args -> {
            // Ensure upload directory exists
            try {
                Path uploadPath = Paths.get(pizzaUploadProperties.getUploadDir());
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    logger.info("Created upload directory: {}", uploadPath);
                } else {
                    logger.info("Upload directory already exists: {}", uploadPath);
                }
            } catch (Exception e) {
                logger.error("Error creating upload directory", e);
            }
        };
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        // Only initialize toppings here, pizzas will be handled by DatabaseSeeder
        if (toppingRepository.count() == 0) {
            logger.info("Initializing toppings...");

            // Create toppings
            Topping cheese = toppingRepository.save(
                    new Topping("Extra cheese", 150.00)
            );
            Topping pepperoni = toppingRepository.save(
                    new Topping("Pepperoni", 200.00)
            );
            Topping mushrooms = toppingRepository.save(
                    new Topping("Mushrooms", 100.00)
            );
            Topping onions = toppingRepository.save(
                    new Topping("Onions", 75.00)
            );

            logger.info("Created {} toppings", toppingRepository.count());
        } else {
            logger.info("Toppings already initialized, skipping topping initialization.");
        }
    }
}
