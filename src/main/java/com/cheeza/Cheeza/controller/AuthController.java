package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.dto.RegisterRequest;
import com.cheeza.Cheeza.dto.UserRegistrationDto;
import com.cheeza.Cheeza.exception.EmailExistsException;
import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;


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

    // Example controller handling a login POST:
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User user = userService.authenticate(email, password); // your method
        if (user != null) {
            session.setAttribute("user", user); // <- THIS LINE IS CRUCIAL
            return "redirect:/"; // or wherever after login
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid login");
            return "redirect:/login";
        }
    }
    
    //----------------
    //REGISTER ENDPOINTS
    //----------------

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Validate passwords match (custom validation)
        if (!registrationDto.isPasswordsMatch()) {
            result.rejectValue("confirmPassword", "user.confirmPassword", "Passwords must match");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerNewUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (EmailExistsException e) {
            result.rejectValue("email", "user.email", "Email already in use");
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



    //---------------
    // User Profile
    //---------------

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String showProfile(Model model, Principal principal){
        User user = userService.findByEmail(principal.getName());

        if (user==null){
            return "redirect:/auth/login?error=user_not_found";
        }
        model.addAttribute("user", user);
        return "auth/profile";
    }
    @GetMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public String showEditForm(Model model,Principal principal){
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user",user);
        return "auth/edit-profile";
    }
    @PostMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@ModelAttribute User updatedUser,
                                Principal principal,
                                RedirectAttributes redirectAttributes){
        User currentUser = userService.findByEmail(principal.getName());
        currentUser.updateProfileDetails(
                updatedUser.getFullName(),
                updatedUser.getPhone(),
                updatedUser.getAddress()
        );
        userService.save(currentUser);

        redirectAttributes.addFlashAttribute("success","Profile updated successfully!");
        return "redirect:/auth/profile";

    }
}