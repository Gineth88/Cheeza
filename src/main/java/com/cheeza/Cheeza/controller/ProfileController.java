package com.cheeza.Cheeza.controller;

import com.cheeza.Cheeza.model.User;
import com.cheeza.Cheeza.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user", user);
        return "profile";
    }

//    @PostMapping
//    public String updateProfile(@ModelAttribute User updatedUser, Principal principal) {
//        userService.updateUser(principal.getName(), updatedUser);
//        return "redirect:/profile?updated";
//    }
}
