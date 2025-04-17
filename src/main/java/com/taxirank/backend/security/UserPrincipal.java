package com.taxirank.backend.security;

import com.taxirank.backend.models.User;
import com.taxirank.backend.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private UserRole role;

    public UserPrincipal(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities, UserRole role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.role = role;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getRole()
        );
    }

    public Long getId() {
        return id;
    }
    
    public UserRole getRole() {
        return this.role;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
} 