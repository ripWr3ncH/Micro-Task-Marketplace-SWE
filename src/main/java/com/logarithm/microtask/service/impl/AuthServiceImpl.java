package com.logarithm.microtask.service.impl;

import com.logarithm.microtask.dto.auth.AuthResponse;
import com.logarithm.microtask.dto.auth.LoginRequest;
import com.logarithm.microtask.dto.auth.RegisterRequest;
import com.logarithm.microtask.entity.Role;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.exception.BadRequestException;
import com.logarithm.microtask.repository.RoleRepository;
import com.logarithm.microtask.repository.UserRepository;
import com.logarithm.microtask.security.JwtService;
import com.logarithm.microtask.security.TokenRevocationService;
import com.logarithm.microtask.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
        private final TokenRevocationService tokenRevocationService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use.");
        }

        Set<RoleName> requestedRoles = request.getRoles() == null || request.getRoles().isEmpty()
                ? Set.of(RoleName.BUYER)
                : request.getRoles();

        if (requestedRoles.contains(RoleName.ADMIN)) {
            throw new BadRequestException("ADMIN role cannot be self-assigned.");
        }

        Set<Role> resolvedRoles = new HashSet<>();
        for (RoleName roleName : requestedRoles) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
            resolvedRoles.add(role);
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(resolvedRoles)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials."));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .build();
    }

        @Override
        public void logout(String token) {
                tokenRevocationService.revokeToken(token);
        }
}
