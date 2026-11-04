package com.taxirank.backend.services;

import com.taxirank.backend.models.User;

import java.util.Optional;

/** Profile photos of person accounts */
public interface ProfilePhotoService {
    /** JPEG or PNG up to 5 MB; stored as a square JPEG without metadata (no GPS, no camera details) */
    User upload(Long userId, byte[] image);

    User remove(Long userId);

    /** The stored JPEG, if the user has a photo */
    Optional<byte[]> load(Long userId);
}
