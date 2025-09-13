package com.phuc.auth.service.Impl;

import com.phuc.auth.dto.auth0.Auth0TokenResponse;
import com.phuc.auth.dto.auth0.Auth0UserInfo;
import com.phuc.auth.dto.request.RefreshTokenRequest;
import com.phuc.auth.dto.request.UserCreateRequest;
import com.phuc.auth.dto.request.UserUpdateRequest;
import com.phuc.auth.dto.response.TokenResponse;
import com.phuc.auth.dto.response.UserResponse;
import com.phuc.auth.entity.User;
import com.phuc.auth.exception.AppException;
import com.phuc.auth.exception.ErrorCode;
import com.phuc.auth.httpclient.Auth0Client;
import com.phuc.auth.mapper.UserMapper;
import com.phuc.auth.repository.UserRepository;
import com.phuc.auth.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    Auth0Client auth0Client;

    @Value("${auth0.client-id}")
    @NonFinal
    String clientId;

    @Value("${auth0.client-secret}")
    @NonFinal
    String clientSecret;

    @Value("${auth0.redirect-uri}")
    @NonFinal
    String redirectUri;

    @Value("${auth0.audience}")
    @NonFinal
    String audience;

    @Transactional
    @Override
    public ResponseEntity<TokenResponse> login(String code) {
        if (code == null || code.isEmpty()) {
            log.error("Authorization code is required");
            throw new AppException(ErrorCode.CODE_IS_EMPTY);
        }
        try {
            String formData = String.format(
                    "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&audience=%s&scope=openid%%20profile%%20email%%20offline_access",
                    clientId, clientSecret, code, redirectUri, audience);
            Auth0TokenResponse tokenResponse = auth0Client.exchangeCodeForToken(formData);
            if (tokenResponse.getAccessToken() == null) {
                log.error("Failed to get access token from Auth0");
                throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
            }

            Auth0UserInfo userInfo = auth0Client.getUserInfo("Bearer " + tokenResponse.getAccessToken());
            userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> createNewUSer(userInfo));

            TokenResponse response = TokenResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expiresIn(tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn().toString() : null)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }

    @Transactional
    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            log.error("Refresh token is required");
            throw new AppException(ErrorCode.REFRESH_TOKEN_IS_REQUIRED);
        }

        try {
            String formData = String.format(
                    "grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s&audience=%s&scope=openid%%20profile%%20email%%20offline_access",
                    clientId, clientSecret, request.getRefreshToken(), audience);

            Auth0TokenResponse tokenResponse = auth0Client.refreshToken(formData);
            if (tokenResponse.getAccessToken() == null) {
                log.error("Failed to refresh token");
                throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
            }

            return TokenResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken() != null
                            && !tokenResponse.getRefreshToken().isEmpty()
                            ? tokenResponse.getRefreshToken()
                            : request.getRefreshToken())
                    .expiresIn(tokenResponse.getExpiresIn().toString())
                    .build();
        } catch (Exception e) {
            log.error("Refresh token error: {}", e.getMessage(), e);
            throw new AppException(e.getMessage().contains("invalid_grant")
                    ? ErrorCode.REFRESH_TOKEN_IS_INVALID
                    : ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }

    private User createNewUSer(Auth0UserInfo userInfo) {
        String auth0Id = userInfo.getSub();
        String firstName = userInfo.getGivenName() != null ? userInfo.getGivenName() : "";
        String lastName = userInfo.getFamilyName() != null ? userInfo.getFamilyName() : "";
        String email = userInfo.getEmail();
        String phoneNumber = userInfo.getPhoneNumber() != null ? userInfo.getPhoneNumber() : "";
        String fullName = firstName + " " + lastName;

        User newUser = User.builder()
                .fullName(fullName)
                .email(email)
                .auth0Id(auth0Id)
                .phoneNumber(phoneNumber)
                .avatarUrl(userInfo.getPicture())
                .build();

        userRepository.save(newUser);
        return newUser;
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByUserId(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest request, Long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
