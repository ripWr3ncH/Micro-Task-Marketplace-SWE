package com.logarithm.microtask.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email")
    }
)
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
    )
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "buyer")
    @ToString.Exclude
    private Set<Task> createdTasks = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "seller")
    @ToString.Exclude
    private Set<Application> applications = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "seller")
    @ToString.Exclude
    private Set<TaskAssignment> assignments = new HashSet<>();
}
