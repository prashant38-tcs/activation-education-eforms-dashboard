package com.tcsion.eforms.service;

import com.tcsion.eforms.dto.request.ChangePasswordRequest;
import com.tcsion.eforms.dto.request.UserCreateRequest;
import com.tcsion.eforms.entity.User;
import java.util.List;

public interface UserManagementService {
    User createUser(UserCreateRequest request, String temporaryPassword);
    User toggleActive(Long userId, boolean active);
    List<User> getAllUsers();
    List<User> getActiveDevelopers();
    void changeOwnPassword(Long userId, ChangePasswordRequest request);
    void resetPassword(Long userId, String temporaryPassword);
    void unlockAccount(Long userId);
}
