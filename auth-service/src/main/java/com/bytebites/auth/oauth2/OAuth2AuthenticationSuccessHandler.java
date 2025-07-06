package com.bytebites.auth.oauth2;

import com.bytebites.auth.model.User;
import com.bytebites.auth.service.AuthService;
import com.bytebites.auth.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtService jwtService;

    public OAuth2AuthenticationSuccessHandler(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = getEmail(oAuth2User);
        String name = getName(oAuth2User);

        // Create user if not exists, assign ROLE_CUSTOMER
        User user = authService.registerOAuth2User(email, name);

        // Issue JWT
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        // Return JWT in response (JSON)
        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\",\"type\":\"Bearer\",\"userId\":" + user.getId() + ",\"email\":\"" + user.getEmail() + "\",\"name\":\"" + user.getName() + "\",\"role\":\"" + user.getRole().name() + "\"}");
        response.getWriter().flush();
    }

    private String getEmail(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        } else if (attributes.containsKey("login")) { // GitHub fallback
            return (String) attributes.get("login") + "@github.com";
        }
        return "unknown@unknown.com";
    }

    private String getName(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        if (attributes.containsKey("name")) {
            return (String) attributes.get("name");
        } else if (attributes.containsKey("login")) {
            return (String) attributes.get("login");
        }
        return "Unknown";
    }
} 