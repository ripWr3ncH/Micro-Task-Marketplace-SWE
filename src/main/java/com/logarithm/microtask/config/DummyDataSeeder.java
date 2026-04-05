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
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Order(2)
@RequiredArgsConstructor
@Profile("!test")
public class DummyDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DummyDataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "password123";

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("Alice Johnson",   "alice@demo.com",  RoleName.BUYER);
        seedUser("Bob Martinez",    "bob@demo.com",    RoleName.SELLER);
        seedUser("Carol Williams",  "carol@demo.com",  RoleName.BUYER);
        seedUser("David Chen",      "david@demo.com",  RoleName.SELLER);
    }

    private void seedUser(String fullName, String email, RoleName roleName) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));

        userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .roles(Set.of(role))
                .build());

        log.info("Demo user seeded: {} ({})", email, roleName);
    }
}
