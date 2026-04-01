package com.logarithm.microtask.repository;

import com.logarithm.microtask.entity.Role;
import com.logarithm.microtask.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
