package com.taxirank.backend.services;

import com.taxirank.backend.dto.AccountAdminDtos.CreateUserRequest;
import com.taxirank.backend.dto.AccountAdminDtos.IssuedCredentials;
import com.taxirank.backend.dto.AccountAdminDtos.ResponsiblePersonRequest;
import com.taxirank.backend.dto.AccountAdminDtos.SelfUpdateRequest;
import com.taxirank.backend.dto.AccountAdminDtos.StatusRequest;
import com.taxirank.backend.dto.AccountAdminDtos.UpdateUserRequest;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.models.AccountEvent;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;

import java.util.List;

/**
 * Account management. Admin operations take the acting user's id: nobody can change
 * their own profile or status or reset their own password through them, so a System
 * Administrator can't lock themselves out.
 */
public interface AccountAdminService {
    /** All filters optional; query matches name, email, phone or login */
    List<User> search(String profileCode, AccountStatus status, String query);

    User getUser(Long id);

    List<AccountEvent> history(Long userId);

    IssuedCredentials createUser(CreateUserRequest request, Long actorId);

    /** Creates the shared account of a rank (login = rank code) with a temporary password */
    IssuedCredentials createRankAccount(TaxiRank rank, String responsiblePersonName, String responsiblePersonPhone,
            User actor, String reason);

    User updateUser(Long id, UpdateUserRequest request, Long actorId);

    User updateSelf(Long id, SelfUpdateRequest request);

    IssuedCredentials resetPassword(Long id, Long actorId);

    User setStatus(Long id, StatusRequest request, Long actorId);

    User setResponsiblePerson(Long id, ResponsiblePersonRequest request, Long actorId);
}
