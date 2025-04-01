package com.example.web.security.controller;

import com.example.dto.security.JwtResponse;
import com.example.dto.security.LoginRequest;
import com.example.dto.security.LogoutRequest;
import com.example.exception.InvalidTokenException;
import com.example.security.AuthService;
import com.example.web.utils.JsonApiSuccessUtil;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        JwtResponse response = authService.authenticateUser(request);
        return JsonApiSuccessUtil.ok("Login successful", response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            JwtResponse response = authService.refreshAccessToken(refreshToken);
            return JsonApiSuccessUtil.ok("Token refreshed successfully", response);
        } catch (InvalidTokenException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token", e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody LogoutRequest logoutRequest) {
        authService.logoutUser(logoutRequest.getUsername());
        return JsonApiSuccessUtil.deleted("Logged out successfully");
    }
}
