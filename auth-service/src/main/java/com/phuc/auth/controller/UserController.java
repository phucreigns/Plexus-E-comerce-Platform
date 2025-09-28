package com.phuc.auth.controller;

import com.phuc.auth.dto.ApiResponse;
import com.phuc.auth.dto.request.RefreshTokenRequest;
import com.phuc.auth.dto.request.UserCreateRequest;
import com.phuc.auth.dto.request.UserUpdateRequest;
import com.phuc.auth.dto.response.TokenResponse;
import com.phuc.auth.dto.response.UserResponse;
import com.phuc.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping("/auth/login")
    public ResponseEntity<TokenResponse> login(@RequestParam(required = false) String code) {
        return userService.login(code);
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.<TokenResponse>builder()
                .result(userService.refreshToken(request))
                .build();
    }

    @PutMapping("/reset-password")
    public ApiResponse<Void> resetPassword() {
        userService.resetPassword();
        return ApiResponse.<Void>builder()
                .message("Send reset password email successfully")
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return userService.logout();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("/all")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/update/{userId}")
    public UserResponse updateUser(UserUpdateRequest request, @PathVariable Long userId){
        return userService.updateUser(request, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
    }



}
