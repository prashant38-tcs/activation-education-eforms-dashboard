package com.tcsion.eforms.controller;

import com.tcsion.eforms.dto.request.UserCreateRequest;
import com.tcsion.eforms.repository.RoleRepository;
import com.tcsion.eforms.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.SecureRandom;

@Controller
@RequestMapping("/user-management")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEAM_LEAD','SYSTEM_ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final RoleRepository roleRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#";

    @GetMapping
    public String userManagement(Model model) {
        model.addAttribute("users", userManagementService.getAllUsers());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("userCreateRequest", new UserCreateRequest());
        return "admin/user-management";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute UserCreateRequest request, Model model) {
        String temporaryPassword = generateTemporaryPassword();
        userManagementService.createUser(request, temporaryPassword);
        model.addAttribute("temporaryPasswordMessage",
                "Account created for '" + request.getUsername() + "'. One-time temporary password: " + temporaryPassword
                + " (the user must change this on first login).");
        return "redirect:/user-management?created=true";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id) {
        userManagementService.toggleActive(id, true);
        return "redirect:/user-management";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id) {
        userManagementService.toggleActive(id, false);
        return "redirect:/user-management";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id) {
        userManagementService.resetPassword(id, generateTemporaryPassword());
        return "redirect:/user-management?passwordReset=true";
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id) {
        userManagementService.unlockAccount(id);
        return "redirect:/user-management";
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(14);
        for (int i = 0; i < 14; i++) sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}
