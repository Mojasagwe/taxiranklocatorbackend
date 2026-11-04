package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.MyAccountDtos.DeleteAccountRequest;
import com.taxirank.backend.dto.MyAccountDtos.NotificationSettings;
import com.taxirank.backend.dto.MyAccountDtos.UpdateMyDetailsRequest;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.ApplicationStatus;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.AccountEvent;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.RankApplication;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.AccountEventRepository;
import com.taxirank.backend.repositories.RankApplicationRepository;
import com.taxirank.backend.repositories.TripRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.security.TemporaryPasswordGenerator;
import com.taxirank.backend.services.MyAccountService;
import com.taxirank.backend.storage.PhotoStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class MyAccountServiceImpl implements MyAccountService {

    static final String DELETE_CONFIRMATION = "DELETE";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountEventRepository accountEventRepository;

    @Autowired
    private RankApplicationRepository rankApplicationRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PhotoStorage photoStorage;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TemporaryPasswordGenerator passwordGenerator;

    @Autowired
    private PersonDetails personDetails;

    @Override
    @Transactional
    public User updateDetails(Long userId, UpdateMyDetailsRequest request) {
        User user = person(userId);
        if (personDetails.changesContact(user, request.email(), request.phoneNumber())) {
            requirePassword(user, request.currentPassword(), "Enter your current password to change your email or mobile number");
        }
        personDetails.apply(user, request.firstName(), request.lastName(), request.email(), request.phoneNumber());
        if (Stream.of(request.firstName(), request.lastName(), request.email(), request.phoneNumber()).anyMatch(Objects::nonNull)) {
            accountEventRepository.save(new AccountEvent(user, user, AccountEvent.DETAILS_CHANGED, "By the account holder"));
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSettings notifications(Long userId) {
        return NotificationSettings.from(person(userId));
    }

    @Override
    @Transactional
    public NotificationSettings updateNotifications(Long userId, NotificationSettings settings) {
        User user = person(userId);
        user.setNotifyApplicationUpdates(settings.applicationUpdates());
        user.setNotifyRankChanges(settings.rankChanges());
        user.setNotifyNews(settings.news());
        return NotificationSettings.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = person(userId);
        if (!DELETE_CONFIRMATION.equals(request.confirmation().trim().toUpperCase())) {
            throw new IllegalArgumentException("Type DELETE to confirm");
        }
        requirePassword(user, request.password(), "Password is incorrect");
        if (user.hasProfile(Profile.SYSTEM_ADMIN) && isLastSystemAdmin(user)) {
            throw new IllegalArgumentException("You're the only System Administrator; make someone else one first");
        }

        // Bulk update first: it clears the persistence context, so reload the user after it
        tripRepository.detachFromUser(userId);
        user = person(userId);

        List<RankApplication> applications = rankApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(userId);
        for (RankApplication application : applications) {
            if (application.getStatus().isOpen()) {
                application.setStatus(ApplicationStatus.WITHDRAWN);
            }
            // The rank details they proposed stay; how to reach them and why they applied go
            application.setMotivation(null);
            application.setProposedContactPhone(null);
            application.setProposedContactEmail(null);
        }
        rankApplicationRepository.saveAll(applications);
        photoStorage.delete(user.getProfilePicture());

        user.setFirstName("Deleted user");
        user.setLastName(null);
        user.setEmail(null);
        user.setPhoneNumber(null);
        user.setProfilePicture(null);
        user.setPassword(passwordEncoder.encode(passwordGenerator.generate()));
        user.setNotifyApplicationUpdates(false);
        user.setNotifyRankChanges(false);
        user.setNotifyNews(false);
        user.setAccountStatus(AccountStatus.DELETED);
        user.setStatusReason("Deleted by the account holder");
        user.setDeletedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        user.revokeTokens();
        userRepository.save(user);
        accountEventRepository.save(new AccountEvent(user, user, AccountEvent.ACCOUNT_DELETED, null));
    }

    private User person(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.isRankAccount()) {
            throw new IllegalArgumentException("Rank account details are managed by a System Administrator");
        }
        return user;
    }

    private void requirePassword(User user, String password, String message) {
        if (!StringUtils.hasText(password) || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isLastSystemAdmin(User user) {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(u -> u.hasProfile(Profile.SYSTEM_ADMIN) && !u.isDeleted())
                .allMatch(u -> u.getId().equals(user.getId()));
    }
}
