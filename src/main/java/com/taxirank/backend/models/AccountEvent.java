package com.taxirank.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit trail for an account: who changed what, and when. Shown as "History"
 * on the System Administrator's user detail screen.
 */
@Entity
@Table(name = "account_events")
@Getter
@Setter
@NoArgsConstructor
public class AccountEvent {
    public static final String ACCOUNT_CREATED = "ACCOUNT_CREATED";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String STATUS_CHANGED = "STATUS_CHANGED";
    public static final String PROFILE_CHANGED = "PROFILE_CHANGED";
    public static final String DETAILS_CHANGED = "DETAILS_CHANGED";
    public static final String RESPONSIBLE_PERSON_CHANGED = "RESPONSIBLE_PERSON_CHANGED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Null when the system did it (e.g. bootstrap)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 500)
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AccountEvent(User user, User actor, String action, String details) {
        this.user = user;
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }
}
