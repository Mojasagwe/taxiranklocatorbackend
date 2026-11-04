package com.taxirank.backend.services;

import com.taxirank.backend.dto.MyAccountDtos.DeleteAccountRequest;
import com.taxirank.backend.dto.MyAccountDtos.NotificationSettings;
import com.taxirank.backend.dto.MyAccountDtos.UpdateMyDetailsRequest;
import com.taxirank.backend.models.User;

/** What a signed-in person can do to their own account. Rank accounts are managed by System Administrators. */
public interface MyAccountService {
    User updateDetails(Long userId, UpdateMyDetailsRequest request);

    NotificationSettings notifications(Long userId);

    NotificationSettings updateNotifications(Long userId, NotificationSettings settings);

    /**
     * Erases personal data (POPIA): name, contact details, photo, settings and the link to
     * their trips; withdraws open rank applications; revokes every token. The row stays,
     * anonymised, so audit history and rank records remain consistent. The email and phone
     * number are free to sign up again.
     */
    void deleteAccount(Long userId, DeleteAccountRequest request);
}
