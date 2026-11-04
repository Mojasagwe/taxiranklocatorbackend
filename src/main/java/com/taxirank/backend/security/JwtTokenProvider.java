package com.taxirank.backend.security;

import com.taxirank.backend.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    // Must equal users.token_version for the token to be accepted; see User#revokeTokens
    static final String VERSION_CLAIM = "ver";

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    /** The parts of a valid token the filter needs */
    public record TokenClaims(Long userId, int tokenVersion) {
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(user.getId()))
                .claim(VERSION_CLAIM, user.getTokenVersion())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /** Empty when the token is missing, malformed, expired or wrongly signed */
    public Optional<TokenClaims> parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Integer version = claims.get(VERSION_CLAIM, Integer.class);
            return Optional.of(new TokenClaims(Long.parseLong(claims.getSubject()), version == null ? 0 : version));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
