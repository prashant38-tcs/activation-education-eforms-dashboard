package com.tcsion.eforms.controller;

import com.tcsion.eforms.dto.request.ChangePasswordRequest;
import com.tcsion.eforms.entity.PasswordResetToken;
import com.tcsion.eforms.entity.User;
import com.tcsion.eforms.repository.PasswordResetTokenRepository;
import com.tcsion.eforms.repository.UserRepository;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserManagementService userManagementService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             @RequestParam(required = false) String expired,
                             Model model) {
        if (error != null) {
            String message;
            switch (error) {
                case "locked":
                    message = "Your account has been temporarily locked due to repeated failed login attempts. Please try again later or contact your administrator.";
                    break;
                case "disabled":
                    message = "Your account is inactive. Please contact your administrator.";
                    break;
                default:
                    message = "Invalid username or password.";
            }
            model.addAttribute("errorMessage", message);
        }
        if (logout != null) model.addAttribute("infoMessage", "You have been securely logged out.");
        if (expired != null) model.addAttribute("infoMessage", "Your session has expired. Please log in again.");
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestReset(@RequestParam String username, Model model) {
        userRepository.findByUsernameIgnoreCase(username).filter(User::isActive).ifPresent(user -> {
            PasswordResetToken token = PasswordResetToken.builder()
                    .user(user).token(UUID.randomUUID().toString())
                    .expiryAt(LocalDateTime.now().plusHours(2))
                    .build();
            passwordResetTokenRepository.save(token);
        });
        model.addAttribute("infoMessage",
                "If this username is registered, password reset instructions have been generated. Please contact your Team Lead or System Administrator to complete the reset.");
        return "forgot-password";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(@RequestParam(required = false) Boolean firstLogin, Model model) {
        model.addAttribute("firstLogin", Boolean.TRUE.equals(firstLogin));
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordRequest request, Model model) {
        Long userId = SecurityUtils.currentUserId();
        try {
            userManagementService.changeOwnPassword(userId, request);
            return "redirect:/login?passwordChanged=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "change-password";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
