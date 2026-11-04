package com.taxirank.backend.security;

import com.taxirank.backend.enums.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Ownership check for rank-scoped writes, used as
 * {@code @PreAuthorize("@rankAccess.canManage(authentication, #rankId)")}.
 * RANKS_MANAGE covers every rank; RANK_MANAGE_OWN only the account's own rank.
 */
@Component("rankAccess")
public class RankAccess {

    public boolean canManage(Authentication authentication, Long rankId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        if (hasAuthority(authentication, Permission.RANKS_MANAGE)) {
            return true;
        }
        return hasAuthority(authentication, Permission.RANK_MANAGE_OWN)
                && rankId != null
                && rankId.equals(principal.getRankId());
    }

    private static boolean hasAuthority(Authentication authentication, Permission permission) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(permission.name()));
    }
}
