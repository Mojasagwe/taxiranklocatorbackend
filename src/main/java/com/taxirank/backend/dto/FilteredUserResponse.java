package com.taxirank.backend.dto;

public class FilteredUserResponse {
    private final UserDetailsDTO user;
    private final String token;

    public FilteredUserResponse(UserDetailsDTO user, String token) {
        this.user = user;
        this.token = token;
    }

    public UserDetailsDTO getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
} 