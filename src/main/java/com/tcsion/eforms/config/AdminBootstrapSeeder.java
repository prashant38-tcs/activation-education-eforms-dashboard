package com.tcsion.eforms.config;

import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.entity.User;
import com.tcsion.eforms.repository.RoleRepository;
import com.tcsion.eforms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Controlled initialization process for the initial named administrators
 * (Section 3: create administrators through seed data or a controlled
 * initialization process; do not use publicly visible default passwords).
 *
 * Runs on every startup but is fully idempotent - only creates a user if
 * the username does not already exist, and never resets an existing user's
 * password. Passwords are never hardcoded:
 *   - If SEED_<USERNAME>_PASSWORD env var is supplied, that value is used.
 *   - Otherwise a cryptographically random 16-character password is
 *     generated and printed ONCE to the log at WARN level.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
public class AdminBootstrapSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    private static final String ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUser("sanjay.singh", "Sanjay Singh", "sanjay.singh@tcsion.example", Role.TEAM_LEAD);
        seedUser("prashant.chaturvedi", "Prashant Chaturvedi", "prashant.chaturvedi@tcsion.example", Role.TECHNICAL_LEAD);
        seedUser("pooja.gehlod", "Pooja Gehlod", "pooja.gehlod@tcsion.example", Role.TECHNICAL_LEAD);
        seedUser("mayurika.srivastava", "Mayurika Srivastava", "mayurika.srivastava@tcsion.example", Role.DASHBOARD_HANDLER);
    }

    private void seedUser(String username, String fullName, String email, String roleCode) {
        if (userRepository.existsByUsernameIgnoreCase(username)) return;
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Required role not seeded: " + roleCode));

        String envKey = "SEED_" + username.toUpperCase().replace('.', '_') + "_PASSWORD";
        String password = Optional.ofNullable(environment.getProperty(envKey))
                .orElseGet(this::generateSecurePassword);

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(username).fullName(fullName).email(email)
                .passwordHash(passwordEncoder.encode(password))
                .active(true).forcePasswordChange(true)
                .roles(roles)
                .build();
        userRepository.save(user);

        if (!environment.containsProperty(envKey)) {
            log.warn("=====================================================================");
            log.warn("Bootstrapped initial account for '{}' ({}) with a ONE-TIME generated password.", username, roleCode);
            log.warn("Temporary password: {}", password);
            log.warn("This password is shown only once and MUST be changed on first login.");
            log.warn("To control this password explicitly next time, set environment variable {}", envKey);
            log.warn("=====================================================================");
        } else {
            log.info("Bootstrapped initial account for '{}' ({}) using the configured {} value.", username, roleCode, envKey);
        }
    }

    private String generateSecurePassword() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}
