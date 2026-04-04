package com.logarithm.microtask.service.impl;

import com.logarithm.microtask.dto.auth.LoginRequest;
import com.logarithm.microtask.dto.auth.RegisterRequest;
import com.logarithm.microtask.entity.Role;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.exception.BadRequestException;
import com.logarithm.microtask.repository.RoleRepository;
import com.logarithm.microtask.repository.UserRepository;
import com.logarithm.microtask.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@test.com")
                .password("encoded")
                .roles("BUYER")
                .build();
    }

    @Test
    void registerShouldSucceedWithDefaultRole() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("User")
                .email("user@test.com")
                .password("secret123")
                .build();

        Role buyerRole = Role.builder().name(RoleName.BUYER).build();

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.BUYER)).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        var response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token");
        assertThat(response.getRoles()).contains("BUYER");
    }

    @Test
    void registerShouldThrowWhenEmailExists() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(RegisterRequest.builder().email("user@test.com").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void registerShouldCreateRoleWhenMissing() {
        RegisterRequest request = RegisterRequest.builder()
            .fullName("Seller")
            .email("seller@test.com")
                .password("secret123")
            .roles(Set.of(RoleName.SELLER))
                .build();

        Role sellerRole = Role.builder().name(RoleName.SELLER).build();

        when(userRepository.existsByEmail("seller@test.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.SELLER)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(sellerRole);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userDetailsService.loadUserByUsername("seller@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        var response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void registerShouldRejectAdminSelfAssignment() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Admin")
                .email("admin@test.com")
                .password("secret123")
                .roles(Set.of(RoleName.ADMIN))
                .build();

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN role cannot be self-assigned");
    }

    @Test
    void loginShouldSucceed() {
        User user = User.builder().email("user@test.com").password("encoded").roles(Set.of(Role.builder().name(RoleName.BUYER).build())).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        var response = authService.login(LoginRequest.builder().email("user@test.com").password("secret").build());

        assertThat(response.getToken()).isEqualTo("token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginShouldThrowWhenUserMissingAfterAuth() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(LoginRequest.builder().email("missing@test.com").password("secret").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void loginShouldIncludeRoles() {
        User user = User.builder().email("user@test.com").password("encoded").roles(Set.of(Role.builder().name(RoleName.SELLER).build())).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        var response = authService.login(LoginRequest.builder().email("user@test.com").password("secret").build());

        assertThat(response.getRoles()).contains("SELLER");
    }
}
