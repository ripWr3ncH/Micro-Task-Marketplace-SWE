package com.logarithm.microtask.dto.auth;

import com.logarithm.microtask.entity.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @NotBlank
    @Size(min = 8, max = 120)
    private String password;

    private Set<RoleName> roles;
}
