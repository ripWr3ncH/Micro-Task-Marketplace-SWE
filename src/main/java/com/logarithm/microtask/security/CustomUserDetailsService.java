package com.logarithm.microtask.security;

import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.exception.ResourceNotFoundException;
import com.logarithm.microtask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(
                        user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                                .collect(Collectors.toSet())
                )
                .build();
    }
}
