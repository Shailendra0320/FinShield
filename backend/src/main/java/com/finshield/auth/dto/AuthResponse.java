package com.finshield.auth.dto;

public record AuthResponse(String token, String username, String role, String fullName) {}