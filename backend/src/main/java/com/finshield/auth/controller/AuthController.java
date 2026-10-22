package com.finshield.auth.controller;

import com.finshield.auth.dto.AuthRequest;
import com.finshield.auth.dto.AuthResponse;
import com.finshield.auth.model.User;
import com.finshield.auth.repository.UserRepository;
import com.finshield.auth.security.JwtUtil;
import com.finshield.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        AuthResponse response = new AuthResponse(token, user.getUsername(), user.getRole().name(), user.getFullName());
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}