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


}