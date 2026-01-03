package com.example.learningjava.auth.service;

import com.example.learningjava.auth.dto.AuthResponse;
import com.example.learningjava.auth.dto.LoginRequest;
import com.example.learningjava.auth.dto.RefreshRequest;
import com.example.learningjava.auth.dto.RegisterRequest;
import com.example.learningjava.auth.exception.InvalidCredentialsException;
import com.example.learningjava.auth.model.RefreshToken;
import com.example.learningjava.auth.util.JwtUtil;
import com.example.learningjava.model.User;
import com.example.learningjava.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public void register(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken =
                refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshRequest request) {

        RefreshToken token =
                refreshTokenService.validateRefreshToken(request.getRefreshToken());

        User user = userRepository.findById(token.getUserId())
                .orElseThrow();

        String newAccessToken = jwtUtil.generateAccessToken(user);

        return new AuthResponse(newAccessToken, request.getRefreshToken());
    }

    public void logout(RefreshRequest req) {

        RefreshToken token =
                refreshTokenService.validateRefreshToken(req.getRefreshToken());

        refreshTokenService.revokeToken(token);
    }
}
