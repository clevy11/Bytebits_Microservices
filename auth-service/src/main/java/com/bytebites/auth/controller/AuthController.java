package com.bytebites.auth.controller;

import com.bytebites.auth.dto.AuthResponse;
import com.bytebites.auth.dto.LoginRequest;
import com.bytebites.auth.dto.RegisterRequest;
import com.bytebites.auth.model.User;
import com.bytebites.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return 403 Forbidden for authentication failures
            return ResponseEntity.status(403).build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String token) {
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user with specified role (Admin only)")
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (Admin only)")
    public ResponseEntity<String> getAllOrders() {
        // This would typically call the order service
        return ResponseEntity.ok("Admin orders endpoint - would call order service");
    }

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String googleAuthUri;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/oauth2/google")
    @Operation(
            summary = "Redirect to Google OAuth2 login",
            description = "Redirects directly to Google's OAuth2 login page. After successful authentication, " +
                    "the user will be redirected back to the application with a JSON response containing a JWT token. " +
                    "No HTML login page will be displayed."
    )
    public void redirectToGoogleOAuth2Login(HttpServletResponse response) throws IOException {
        String redirectUri = "http://localhost:" + serverPort + "/login/oauth2/code/google";
        String scope = "profile email";

        String authUrl = googleAuthUri +
                "?client_id=" + googleClientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scope;

        response.sendRedirect(authUrl);
    }
}
