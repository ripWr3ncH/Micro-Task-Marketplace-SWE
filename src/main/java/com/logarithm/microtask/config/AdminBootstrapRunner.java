package com.logarithm.microtask.config;

import com.logarithm.microtask.entity.Role;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.repository.RoleRepository;
import com.logarithm.microtask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_FULL_NAME = "System Admin";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        User adminUser = userRepository.findByEmail(ADMIN_EMAIL)
                .orElseGet(() -> User.builder()
                        .fullName(ADMIN_FULL_NAME)
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(new HashSet<>())
                        .build());

        if (adminUser.getRoles() == null) {
            adminUser.setRoles(new HashSet<>());
        }

        adminUser.getRoles().add(adminRole);
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));

        if (adminUser.getFullName() == null || adminUser.getFullName().isBlank()) {
            adminUser.setFullName(ADMIN_FULL_NAME);
        }

        userRepository.save(adminUser);
        log.info("Admin bootstrap ensured for {}", ADMIN_EMAIL);
    }
}
