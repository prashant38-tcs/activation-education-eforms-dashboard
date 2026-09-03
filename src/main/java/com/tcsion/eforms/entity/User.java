package com.tcsion.eforms.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", unique = true, length = 40)
    private String employeeCode;

    @Column(name = "username", nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "force_password_change", nullable = false)
    @Builder.Default
    private boolean forcePasswordChange = true;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public boolean hasRole(String roleCode) {
        return roles.stream().anyMatch(r -> r.getRoleCode().equals(roleCode));
    }
}
