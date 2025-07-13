package com.bytebites.order.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("No JWT token found in request headers");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid Authorization header");
            return;
        }

        try {
            String token = header.replace("Bearer ", "");
            log.debug("Attempting to validate JWT token");
            
            // Parse JWT token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Extract username and roles
            String username = claims.getSubject();
            if (username == null || username.trim().isEmpty()) {
                log.error("Username is null or empty in JWT token");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token: missing username");
                return;
            }

            // Process roles
            List<String> roles = extractRoles(claims);
            if (roles.isEmpty()) {
                log.warn("No valid roles found in JWT token for user: {}", username);
                response.sendError(HttpStatus.FORBIDDEN.value(), "No valid roles found in token");
                return;
            }

            // Create authentication token
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Create authentication with user ID as the principal
            // The username in the JWT should be the user's ID
            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authentication set in SecurityContext for user ID: {} with roles: {}", username, roles);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token has expired");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error processing authentication");
        }
    }
    
    private List<String> extractRoles(Claims claims) {
        Object rolesClaim = claims.get("roles");
        List<String> roles = new ArrayList<>();
        
        if (rolesClaim == null) {
            return roles;
        }
        
        try {
            if (rolesClaim instanceof String) {
                String roleStr = ((String) rolesClaim).trim();
                if (!roleStr.isEmpty()) {
                    roles.add(roleStr);
                }
            } else if (rolesClaim instanceof List) {
                roles = ((List<?>) rolesClaim).stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to parse roles claim: {}", e.getMessage());
        }
        
        return roles;
    }
}
