package com.taxirank.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.UserDTO;
import com.taxirank.backend.dto.UserDetailsDTO;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}
	
	@GetMapping("/by-role/{role}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
		return ResponseEntity.ok(userService.getUsersByRole(role));
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('SUPER_ADMIN') or #id == authentication.principal.id")
	public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable Long id) {
		// Get the authentication object from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Get the viewer's role
        UserRole viewerRole = UserRole.RIDER; // Default to RIDER
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            viewerRole = userPrincipal.getRole();
        }
        
        // Get user details with role-based filtering
        return ResponseEntity.ok(userService.getUserDetailsByIdWithRoleFilter(id, viewerRole));
	}
	
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #id == authentication.principal.id")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
		return ResponseEntity.ok(userService.updateUser(id, userDTO));
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
} 