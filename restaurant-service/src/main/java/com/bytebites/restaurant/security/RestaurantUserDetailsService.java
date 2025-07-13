package com.bytebites.restaurant.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RestaurantUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // In a real application, you would fetch the user from your database
        // For now, we'll create a simple user with the provided username and a dummy password
        // The actual password check is not needed since we're using JWT
        return new User(
            username,
            "", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // Default role
        );
    }
}
