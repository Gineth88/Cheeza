package com.cheeza.Cheeza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String showHomePage(Model model){
        model.addAttribute("welcomeMessage","Welcome to chezza Pizza!");
        model.addAttribute("featuredSpecial","Today's Special: Magheritta Pizza - LKR 1500.00");
        return "home";
    }
}
