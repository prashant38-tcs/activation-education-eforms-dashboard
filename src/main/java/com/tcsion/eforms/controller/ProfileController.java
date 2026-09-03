package com.tcsion.eforms.controller;

import com.tcsion.eforms.repository.UserRepository;
import com.tcsion.eforms.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping
    public String profile(Model model) {
        userRepository.findById(SecurityUtils.currentUserId()).ifPresent(u -> model.addAttribute("user", u));
        return "profile";
    }
}
