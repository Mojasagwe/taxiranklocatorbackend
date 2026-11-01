package com.taxirank.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.taxirank.backend.dto.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    // @PreAuthorize denials must stay 403; the catch-all below would otherwise turn them into 500s
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        String error = "Error: " + ex.getMessage();
        ex.printStackTrace(); // For debugging
        return ResponseEntity.internalServerError().body(error);
    }
}
