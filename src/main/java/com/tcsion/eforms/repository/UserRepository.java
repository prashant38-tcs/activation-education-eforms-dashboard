package com.tcsion.eforms.repository;

import com.tcsion.eforms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByFullNameIgnoreCase(String fullName);
    boolean existsByUsernameIgnoreCase(String username);
    List<User> findByActiveTrue();
    List<User> findByRoles_RoleCodeAndActiveTrue(String roleCode);
}
