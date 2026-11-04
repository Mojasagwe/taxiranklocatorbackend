package com.taxirank.backend.security;

import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Authenticated account. Authorities are the profile's permission names
 * (e.g. "USERS_MANAGE") plus "PROFILE_<code>" for display-level checks.
 */
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String loginName;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String profileCode;
    private final Long rankId;
    private final AccountStatus accountStatus;
    private final boolean mustChangePassword;
    private final int tokenVersion;

    private UserPrincipal(Long id, String loginName, String password, Collection<? extends GrantedAuthority> authorities,
            String profileCode, Long rankId, AccountStatus accountStatus, boolean mustChangePassword, int tokenVersion) {
        this.id = id;
        this.loginName = loginName;
        this.password = password;
        this.authorities = authorities;
        this.profileCode = profileCode;
        this.rankId = rankId;
        this.accountStatus = accountStatus;
        this.mustChangePassword = mustChangePassword;
        this.tokenVersion = tokenVersion;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        user.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.name())));
        authorities.add(new SimpleGrantedAuthority("PROFILE_" + user.getProfile().getCode()));

        return new UserPrincipal(
                user.getId(),
                user.getLoginName(),
                user.getPassword(),
                authorities,
                user.getProfile().getCode(),
                user.isRankAccount() ? user.getRank().getId() : null,
                user.getAccountStatus(),
                user.isMustChangePassword(),
                user.getTokenVersion()
        );
    }

    public Long getId() {
        return id;
    }

    public String getProfileCode() {
        return profileCode;
    }

    /** The rank this account belongs to, for rank accounts; otherwise null */
    public Long getRankId() {
        return rankId;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    @Override
    public String getUsername() {
        return loginName;
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

    // Suspended and blocked accounts can't log in or use existing tokens
    @Override
    public boolean isAccountNonLocked() {
        return accountStatus == null || accountStatus.canLogIn();
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
