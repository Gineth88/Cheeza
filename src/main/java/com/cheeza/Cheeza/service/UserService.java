package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.RegisterRequest;
import com.cheeza.Cheeza.exception.EmailExistsException;
import com.cheeza.Cheeza.exception.UserNotFoundException;
import com.cheeza.Cheeza.model.Role;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailExistsException(request.getEmail());
        }

        User user = new User.Builder(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        )
                .fullName(request.getFullName())
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

//    public User updateUser(String email, User updatedUser) {
//        User existingUser = userRepository.findByEmail(email)
//                .orElseThrow();
//
//        // Update only allowed fields
//        User updated = existingUser.updateDetails(
//                updateRequest.getFullName(),
//                updateRequest.getPhone(),
//                updateRequest.getAddress()
//        );
//
//        return userRepository.save(updated);
//    }
}
