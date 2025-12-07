package com.phuc.auth.service;

import com.phuc.auth.dto.request.RefreshTokenRequest;
import com.phuc.auth.dto.request.UserUpdateRequest;
import com.phuc.auth.dto.response.TokenResponse;
import com.phuc.auth.dto.response.UserResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    ResponseEntity<TokenResponse> login(String code);

    TokenResponse refreshToken(RefreshTokenRequest request);

    @Transactional
    void resetPassword();

    @Transactional
    ResponseEntity<Void> logout();

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();
    UserResponse updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long id);

}
