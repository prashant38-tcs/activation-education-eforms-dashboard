package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.dto.request.ChangePasswordRequest;
import com.tcsion.eforms.dto.request.UserCreateRequest;
import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.entity.User;
import com.tcsion.eforms.exception.BusinessValidationException;
import com.tcsion.eforms.exception.DuplicateResourceException;
import com.tcsion.eforms.exception.ResourceNotFoundException;
import com.tcsion.eforms.repository.RoleRepository;
import com.tcsion.eforms.repository.UserRepository;
import com.tcsion.eforms.service.AuditService;
import com.tcsion.eforms.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    @Transactional
    public User createUser(UserCreateRequest request, String temporaryPassword) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already in use.");
        }
        Set<Role> roles = request.getRoleCodes().stream()
                .map(code -> roleRepository.findByRoleCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + code)))
                .collect(Collectors.toSet());

        User user = User.builder()
                .employeeCode(request.getEmployeeCode())
                .username(request.getUsername().trim())
                .fullName(request.getFullName().trim())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .active(true)
                .forcePasswordChange(true)
                .roles(new HashSet<>(roles))
                .build();
        user = userRepository.save(user);
        auditService.log("USER_CREATED", "USER", user.getId(), user.getUsername(), null,
                "roles=" + request.getRoleCodes());
        return user;
    }

    @Override
    @Transactional
    public User toggleActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        boolean oldValue = user.isActive();
        user.setActive(active);
        if (!active) user.setAccountLocked(true);
        userRepository.save(user);
        auditService.log(active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "USER", user.getId(), user.getUsername(),
                String.valueOf(oldValue), String.valueOf(active));
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveDevelopers() {
        return userRepository.findByRoles_RoleCodeAndActiveTrue(Role.DEVELOPER);
    }

    @Override
    @Transactional
    public void changeOwnPassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessValidationException("New password and confirmation do not match.");
        }
        if (!user.isForcePasswordChange()
                && (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))) {
            throw new BusinessValidationException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordChange(false);
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        auditService.log("PASSWORD_CHANGED", "USER", user.getId(), user.getUsername(), null, null);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String temporaryPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setForcePasswordChange(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        auditService.log("PASSWORD_RESET_BY_ADMIN", "USER", user.getId(), user.getUsername(), null, null);
    }

    @Override
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
        auditService.log("ACCOUNT_UNLOCKED", "USER", user.getId(), user.getUsername(), null, null);
    }
}
