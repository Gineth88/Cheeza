package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.dto.RegisterRequest;
import com.cheeza.Cheeza.dto.UserRegistrationDto;
import com.cheeza.Cheeza.exception.EmailExistsException;
import com.cheeza.Cheeza.exception.UserNotFoundException;
import com.cheeza.Cheeza.model.Notification;
import com.cheeza.Cheeza.model.Role;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request) {
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


       return userRepository.save(user);

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

    public User save(User user) {
        return userRepository.save(user);
    }



    public void registerNewUser(UserRegistrationDto registrationDto) throws EmailExistsException {
        if (emailExists(registrationDto.getEmail())) {
            throw new EmailExistsException("Email already registered");
        }

        User user = new User.Builder(
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword())
        )
                .fullName(registrationDto.getFullName())
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

        @Transactional
        public List<Notification> getUserNotifications(Long userId) {
            User user = userRepository.findById(userId).orElseThrow();
            // Safe: session is open, collection can be loaded
            return user.getNotifications();
        }
    }

