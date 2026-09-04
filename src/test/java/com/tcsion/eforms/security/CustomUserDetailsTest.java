package com.tcsion.eforms.security;

import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Critical test: "Inactive user cannot login". Spring Security's
 * DaoAuthenticationProvider calls UserDetails#isEnabled() (and
 * #isAccountNonLocked()) before allowing authentication to proceed.
 */
class CustomUserDetailsTest {

    @Test
    void inactiveUserIsNotEnabled() {
        User user = User.builder().id(1L).username("inactive.user").fullName("Inactive User")
                .passwordHash("hash").active(false).accountLocked(false)
                .roles(Collections.singleton(Role.builder().roleCode(Role.DEVELOPER).roleName("Developer").build()))
                .build();

        CustomUserDetails details = new CustomUserDetails(user);
        assertFalse(details.isEnabled(), "An inactive user must never be treated as enabled by Spring Security.");
    }

    @Test
    void activeUserIsEnabled() {
        User user = User.builder().id(2L).username("active.user").fullName("Active User")
                .passwordHash("hash").active(true).accountLocked(false)
                .roles(Collections.singleton(Role.builder().roleCode(Role.DEVELOPER).roleName("Developer").build()))
                .build();

        CustomUserDetails details = new CustomUserDetails(user);
        assertTrue(details.isEnabled());
    }

    @Test
    void lockedUserIsNotAccountNonLocked() {
        User user = User.builder().id(3L).username("locked.user").fullName("Locked User")
                .passwordHash("hash").active(true).accountLocked(true)
                .roles(Collections.singleton(Role.builder().roleCode(Role.DEVELOPER).roleName("Developer").build()))
                .build();

        CustomUserDetails details = new CustomUserDetails(user);
        assertFalse(details.isAccountNonLocked(), "A locked account must never be treated as non-locked.");
    }

    @Test
    void authoritiesArePrefixedWithRole() {
        User user = User.builder().id(4L).username("lead.user").fullName("Lead User")
                .passwordHash("hash").active(true)
                .roles(Collections.singleton(Role.builder().roleCode(Role.TEAM_LEAD).roleName("Team Lead").build()))
                .build();

        CustomUserDetails details = new CustomUserDetails(user);
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEAM_LEAD")));
    }
}
