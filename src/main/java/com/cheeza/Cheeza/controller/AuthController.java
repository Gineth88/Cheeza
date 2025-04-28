package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.RegisterRequest;
import com.cheeza.Cheeza.exception.EmailExistsException;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.service.UserService;
import jakarta.validation.Valid;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    //-------------
    //LOGIN ENDPOINT
    //-------------
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
    
    //----------------
    //REGISTER ENDPOINTS
    //----------------

    @GetMapping("/register") 
    public String showRegistrationForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register"; 
    }

     @PostMapping("/register") 
    public String handleRegistration(
        @Valid @ModelAttribute("registerRequest") RegisterRequest request,
        BindingResult bindingResult,
        Model model
    ) {
        
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            
            userService.registerUser(request);
            return "redirect:/auth/login?success"; // Redirect to login on success
        } catch (EmailExistsException e) {
            // Handle duplicate email error
            model.addAttribute("error", "Email already registered");
            return "auth/register";
        }
    }

     // Add user listing for admins
    @GetMapping("/userlist")
    public String showUserList(Model model, 
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Admin access required");
        }
        
        model.addAttribute("users", userService.getAllUsers());
        return "auth/user-list";
    }

    // Add profile endpoint
    @GetMapping("/profile")
    public String showUserProfile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", userService.getUserById(user.getId()));
        return "auth/profile";
    }
}