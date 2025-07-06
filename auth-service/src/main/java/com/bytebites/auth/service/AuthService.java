package com.bytebites.auth.service;

import com.bytebites.auth.dto.AuthResponse;
import com.bytebites.auth.dto.LoginRequest;
import com.bytebites.auth.dto.RegisterRequest;
import com.bytebites.auth.model.User;
import com.bytebites.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(User.Role.valueOf(request.getRole()));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getId()
        );

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User registerOAuth2User(String email, String name) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setPassword(""); // No password for OAuth2
            user.setName(name);
            user.setRole(User.Role.ROLE_CUSTOMER);
            user.setEnabled(true);
            return userRepository.save(user);
        });
    }
} 