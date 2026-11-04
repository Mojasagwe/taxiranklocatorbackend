package com.taxirank.backend.services.impl;

import com.taxirank.backend.exceptions.ConflictException;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Name and contact changes for person accounts, shared by the System Administrator's
 * user management and the user's own profile. Emails are stored lower-case and both
 * email and phone must be unique.
 */
@Component
class PersonDetails {

    @Autowired
    private UserRepository userRepository;

    /** Null fields are left unchanged */
    void apply(User user, String firstName, String lastName, String email, String phoneNumber) {
        if (firstName != null) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null) {
            user.setLastName(lastName.trim());
        }
        if (email != null) {
            requireUniqueEmail(email, user);
            user.setEmail(email.trim().toLowerCase(Locale.ROOT));
        }
        if (phoneNumber != null) {
            requireUniquePhone(phoneNumber, user);
            user.setPhoneNumber(phoneNumber.trim());
        }
    }

    /** True when applying these values would change how the account is reached */
    boolean changesContact(User user, String email, String phoneNumber) {
        boolean emailChanges = email != null && !email.trim().equalsIgnoreCase(String.valueOf(user.getEmail()));
        boolean phoneChanges = phoneNumber != null && !phoneNumber.trim().equals(user.getPhoneNumber());
        return emailChanges || phoneChanges;
    }

    void requireUniqueEmail(String email, User self) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(other -> self == null || !other.getId().equals(self.getId()))
                .ifPresent(other -> {
                    throw new ConflictException("An account with this email already exists");
                });
    }

    void requireUniquePhone(String phoneNumber, User self) {
        boolean taken = userRepository.existsByPhoneNumber(phoneNumber.trim())
                && (self == null || !phoneNumber.trim().equals(self.getPhoneNumber()));
        if (taken) {
            throw new ConflictException("An account with this phone number already exists");
        }
    }
}
