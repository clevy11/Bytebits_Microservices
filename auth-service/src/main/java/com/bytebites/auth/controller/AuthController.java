package com.bytebites.auth.controller;

import com.bytebites.auth.dto.AuthResponse;
import com.bytebites.auth.dto.LoginRequest;
import com.bytebites.auth.dto.RegisterRequest;
import com.bytebites.auth.model.User;
import com.bytebites.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (Admin only)")
    public ResponseEntity<String> getAllOrders() {
        // This would typically call the order service
        return ResponseEntity.ok("Admin orders endpoint - would call order service");
    }
} 