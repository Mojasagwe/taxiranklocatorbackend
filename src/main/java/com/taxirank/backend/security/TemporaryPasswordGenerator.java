package com.taxirank.backend.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * One-time passwords for new rank accounts and admin resets, e.g. "Kx7m-Q2pR-9Tzc".
 * 12 characters from a 54-symbol alphabet (~69 bits); look-alike characters
 * (0/O, 1/l/I) are left out because these are read out over the phone.
 */
@Component
public class TemporaryPasswordGenerator {
    private static final char[] ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789".toCharArray();
    private static final int LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder password = new StringBuilder(LENGTH + 2);
        for (int i = 0; i < LENGTH; i++) {
            if (i > 0 && i % 4 == 0) {
                password.append('-');
            }
            password.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return password.toString();
    }
}
