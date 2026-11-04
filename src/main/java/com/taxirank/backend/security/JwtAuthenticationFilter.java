package com.taxirank.backend.security;

import com.taxirank.backend.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // While a temporary password is in force, only these endpoints work
    private static final Set<String> PASSWORD_CHANGE_PATHS = Set.of("/api/auth/me", "/api/auth/change-password");

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        UserPrincipal principal = null;
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                JwtTokenProvider.TokenClaims claims = tokenProvider.parse(jwt).orElse(null);
                if (claims != null) {
                    UserPrincipal loaded = (UserPrincipal) userDetailsService.loadUserById(claims.userId());
                    // Tokens issued before a password change, reset or deletion are revoked
                    if (loaded.getTokenVersion() == claims.tokenVersion()) {
                        principal = loaded;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // Tokens issued before a suspension or block stop working immediately
        if (principal != null && principal.isAccountNonLocked()) {
            if (principal.isMustChangePassword() && !PASSWORD_CHANGE_PATHS.contains(request.getRequestURI())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"success\":false,\"error\":\"PASSWORD_CHANGE_REQUIRED\"}");
                return;
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
