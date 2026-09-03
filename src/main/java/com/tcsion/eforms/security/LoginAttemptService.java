package com.tcsion.eforms.security;

import com.tcsion.eforms.entity.User;
import com.tcsion.eforms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Transactional
    public void onSuccessfulLogin(String username, String ipAddress) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockTime(null);
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(ipAddress);
            userRepository.save(user);
        });
    }

    @Transactional
    public void onFailedLogin(String username) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            if (user.isAccountLocked() && user.getLockTime() != null
                    && user.getLockTime().plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now())) {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
            }
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxFailedAttempts) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
        });
    }

    public boolean isCurrentlyLocked(User user) {
        if (!user.isAccountLocked()) return false;
        if (user.getLockTime() == null) return true;
        return user.getLockTime().plusMinutes(lockDurationMinutes).isAfter(LocalDateTime.now());
    }
}
