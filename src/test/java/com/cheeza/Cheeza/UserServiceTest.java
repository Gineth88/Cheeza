package com.cheeza.Cheeza;

import com.cheeza.Cheeza.dto.RegisterRequest;
import com.cheeza.Cheeza.repository.UserRepository;
import com.cheeza.Cheeza.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegisterUser() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        userService.registerUser(request);

        // Verify user is saved
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }
}
