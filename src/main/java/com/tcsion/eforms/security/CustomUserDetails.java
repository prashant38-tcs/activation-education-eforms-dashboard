package com.tcsion.eforms.security;

import com.tcsion.eforms.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String fullName;
    private final String passwordHash;
    private final boolean active;
    private final boolean accountLocked;
    private final boolean forcePasswordChange;
    private final Set<String> roleCodes;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.passwordHash = user.getPasswordHash();
        this.active = user.isActive();
        this.accountLocked = user.isAccountLocked();
        this.forcePasswordChange = user.isForcePasswordChange();
        this.roleCodes = user.getRoles().stream().map(r -> r.getRoleCode()).collect(Collectors.toSet());
    }

    public boolean hasRole(String roleCode) {
        return roleCodes.contains(roleCode);
    }

    @Override
    public Set<? extends GrantedAuthority> getAuthorities() {
        return roleCodes.stream().map(rc -> new SimpleGrantedAuthority("ROLE_" + rc)).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() { return passwordHash; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return !accountLocked; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return active; }
}
