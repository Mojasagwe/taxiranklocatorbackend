package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.AccountAdminDtos.CreateUserRequest;
import com.taxirank.backend.dto.AccountAdminDtos.IssuedCredentials;
import com.taxirank.backend.dto.AccountAdminDtos.ResponsiblePersonRequest;
import com.taxirank.backend.dto.AccountAdminDtos.SelfUpdateRequest;
import com.taxirank.backend.dto.AccountAdminDtos.StatusRequest;
import com.taxirank.backend.dto.AccountAdminDtos.UpdateUserRequest;
import com.taxirank.backend.dto.UserResponse;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.exceptions.ConflictException;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.AccountEvent;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.AccountEventRepository;
import com.taxirank.backend.repositories.ProfileRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.security.TemporaryPasswordGenerator;
import com.taxirank.backend.services.AccountAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class AccountAdminServiceImpl implements AccountAdminService {

    // Profiles a person account can be moved between; rank accounts are created only with a rank
    private static final Set<String> PERSON_PROFILES = Set.of(Profile.SYSTEM_ADMIN, Profile.COMMUTER);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    @Autowired
    private AccountEventRepository accountEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TemporaryPasswordGenerator passwordGenerator;

    @Override
    @Transactional(readOnly = true)
    public List<User> search(String profileCode, AccountStatus status, String query) {
        String q = StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : null;
        // Filtered in memory: fine at Travy's size; move to a query when users reach the tens of thousands
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(u -> profileCode == null || u.hasProfile(profileCode))
                .filter(u -> status == null || u.getAccountStatus() == status)
                .filter(u -> q == null || Stream.of(u.getDisplayName(), u.getEmail(), u.getPhoneNumber(), u.getUsername())
                        .filter(Objects::nonNull)
                        .anyMatch(v -> v.toLowerCase(Locale.ROOT).contains(q)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountEvent> history(Long userId) {
        List<AccountEvent> events = accountEventRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId);
        events.forEach(e -> {
            if (e.getActor() != null) {
                e.getActor().getDisplayName(); // load the lazy actor inside the transaction
            }
        });
        return events;
    }

    @Override
    @Transactional
    public IssuedCredentials createUser(CreateUserRequest request, Long actorId) {
        User actor = getUser(actorId);
        if (Profile.RANK_ADMIN.equals(request.profileCode())) {
            if (request.rankId() == null) {
                throw new IllegalArgumentException("rankId is required for a rank account");
            }
            if (!StringUtils.hasText(request.responsiblePersonName()) || !StringUtils.hasText(request.responsiblePersonPhone())) {
                throw new IllegalArgumentException("A rank account needs a responsible person's name and phone");
            }
            TaxiRank rank = taxiRankRepository.findById(request.rankId())
                    .orElseThrow(() -> new NotFoundException("Rank not found"));
            return createRankAccount(rank, request.responsiblePersonName(), request.responsiblePersonPhone(), actor,
                    "Created by " + actor.getDisplayName());
        }

        if (!PERSON_PROFILES.contains(request.profileCode())) {
            throw new IllegalArgumentException("Unknown profile " + request.profileCode());
        }
        if (!StringUtils.hasText(request.firstName()) || !StringUtils.hasText(request.lastName())
                || !StringUtils.hasText(request.email()) || !StringUtils.hasText(request.phoneNumber())) {
            throw new IllegalArgumentException("First name, last name, email and phone are required");
        }
        requireUniqueEmail(request.email(), null);
        requireUniquePhone(request.phoneNumber(), null);

        String temporaryPassword = passwordGenerator.generate();
        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setProfile(profile(request.profileCode()));
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        record(user, actor, AccountEvent.ACCOUNT_CREATED, "Profile " + user.getProfile().getName());
        return new IssuedCredentials(UserResponse.from(user), user.getLoginName(), temporaryPassword);
    }

    @Override
    @Transactional
    public IssuedCredentials createRankAccount(TaxiRank rank, String responsiblePersonName, String responsiblePersonPhone,
            User actor, String reason) {
        if (userRepository.existsByRankId(rank.getId())) {
            throw new ConflictException(rank.getName() + " already has a rank account");
        }
        if (userRepository.existsByUsernameIgnoreCase(rank.getCode())) {
            throw new ConflictException("The login " + rank.getCode() + " is already taken");
        }
        String temporaryPassword = passwordGenerator.generate();
        User account = new User();
        account.setFirstName(rank.getName());
        account.setUsername(rank.getCode());
        account.setRank(rank);
        account.setResponsiblePersonName(responsiblePersonName.trim());
        account.setResponsiblePersonPhone(responsiblePersonPhone.trim());
        account.setProfile(profile(Profile.RANK_ADMIN));
        account.setPassword(passwordEncoder.encode(temporaryPassword));
        account.setMustChangePassword(true);
        userRepository.save(account);
        record(account, actor, AccountEvent.ACCOUNT_CREATED, reason);
        return new IssuedCredentials(UserResponse.from(account), account.getLoginName(), temporaryPassword);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UpdateUserRequest request, Long actorId) {
        User user = getUser(id);
        User actor = getUser(actorId);
        if (user.isRankAccount()) {
            throw new IllegalArgumentException("A rank account's details come from its rank; edit the rank or the responsible person instead");
        }
        applyPersonDetails(user, request.firstName(), request.lastName(), request.email(), request.phoneNumber());

        if (request.profileCode() != null && !user.hasProfile(request.profileCode())) {
            if (user.getId().equals(actorId)) {
                throw new IllegalArgumentException("You can't change your own profile");
            }
            if (!PERSON_PROFILES.contains(request.profileCode())) {
                throw new IllegalArgumentException("A person can only be a System Administrator or a Commuter");
            }
            String previous = user.getProfile().getName();
            user.setProfile(profile(request.profileCode()));
            record(user, actor, AccountEvent.PROFILE_CHANGED, previous + " → " + user.getProfile().getName());
        }
        if (Stream.of(request.firstName(), request.lastName(), request.email(), request.phoneNumber()).anyMatch(Objects::nonNull)) {
            record(user, actor, AccountEvent.DETAILS_CHANGED, null);
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateSelf(Long id, SelfUpdateRequest request) {
        User user = getUser(id);
        if (user.isRankAccount()) {
            throw new IllegalArgumentException("Rank account details are managed by a System Administrator");
        }
        applyPersonDetails(user, request.firstName(), request.lastName(), request.email(), request.phoneNumber());
        if (request.profilePicture() != null) {
            user.setProfilePicture(request.profilePicture());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public IssuedCredentials resetPassword(Long id, Long actorId) {
        if (id.equals(actorId)) {
            throw new IllegalArgumentException("Use change password for your own account");
        }
        User user = getUser(id);
        String temporaryPassword = passwordGenerator.generate();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        record(user, getUser(actorId), AccountEvent.PASSWORD_RESET, null);
        return new IssuedCredentials(UserResponse.from(user), user.getLoginName(), temporaryPassword);
    }

    @Override
    @Transactional
    public User setStatus(Long id, StatusRequest request, Long actorId) {
        if (id.equals(actorId)) {
            throw new IllegalArgumentException("You can't change your own account status");
        }
        boolean hasReason = StringUtils.hasText(request.reason());
        if (request.status() != AccountStatus.ACTIVE && !hasReason) {
            throw new IllegalArgumentException("A reason is required to flag, suspend or block an account");
        }
        User user = getUser(id);
        AccountStatus previous = user.getAccountStatus();
        user.setAccountStatus(request.status());
        user.setStatusReason(request.status() == AccountStatus.ACTIVE ? null : request.reason().trim());
        userRepository.save(user);
        String details = previous + " → " + request.status() + (hasReason ? ": " + request.reason().trim() : "");
        record(user, getUser(actorId), AccountEvent.STATUS_CHANGED, details);
        return user;
    }

    @Override
    @Transactional
    public User setResponsiblePerson(Long id, ResponsiblePersonRequest request, Long actorId) {
        User user = getUser(id);
        if (!user.isRankAccount()) {
            throw new IllegalArgumentException("Only rank accounts have a responsible person");
        }
        String previous = user.getResponsiblePersonName();
        user.setResponsiblePersonName(request.name().trim());
        user.setResponsiblePersonPhone(request.phone().trim());
        userRepository.save(user);
        record(user, getUser(actorId), AccountEvent.RESPONSIBLE_PERSON_CHANGED, previous + " → " + user.getResponsiblePersonName());
        return user;
    }

    private void applyPersonDetails(User user, String firstName, String lastName, String email, String phoneNumber) {
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

    private void requireUniqueEmail(String email, User self) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(other -> self == null || !other.getId().equals(self.getId()))
                .ifPresent(other -> {
                    throw new ConflictException("An account with this email already exists");
                });
    }

    private void requireUniquePhone(String phoneNumber, User self) {
        boolean taken = userRepository.existsByPhoneNumber(phoneNumber.trim())
                && (self == null || !phoneNumber.trim().equals(self.getPhoneNumber()));
        if (taken) {
            throw new ConflictException("An account with this phone number already exists");
        }
    }

    private Profile profile(String code) {
        return profileRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Profile " + code + " is missing; check the Flyway migrations"));
    }

    private void record(User user, User actor, String action, String details) {
        accountEventRepository.save(new AccountEvent(user, actor, action, details));
    }
}
