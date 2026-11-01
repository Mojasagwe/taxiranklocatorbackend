package com.taxirank.backend.models;

import com.taxirank.backend.enums.Permission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * A named set of permissions assigned to users. Profiles are fixed and created
 * by Flyway migrations; codes below are the ones the application relies on.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
public class Profile {
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String RANK_ADMIN = "RANK_ADMIN";
    public static final String COMMUTER = "COMMUTER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_permissions", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "permission", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();
}
