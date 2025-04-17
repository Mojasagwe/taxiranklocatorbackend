package com.taxirank.backend.services.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taxirank.backend.dto.ManagedRankDTO;
import com.taxirank.backend.dto.UserDTO;
import com.taxirank.backend.dto.UserDetailsDTO;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RankAdminService rankAdminService;
	
	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	@Override
	public List<User> getUsersByRole(UserRole role) {
		return userRepository.findByRole(role);
	}
	
	@Override
	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
	}
	
	@Override
	public User createUser(UserDTO userDTO) {
		User user = new User();
		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		user.setEmail(userDTO.getEmail());
		user.setPhoneNumber(userDTO.getPhoneNumber());
		user.setPassword(userDTO.getPassword());
		user.setProfilePicture(userDTO.getProfilePicture());
		user.setAccountStatus(AccountStatus.valueOf(userDTO.getAccountStatus()));
		user.setIsVerified(userDTO.getIsVerified());
		user.setRating(userDTO.getRating());
		user.setTotalTrips(userDTO.getTotalTrips());
		
		// Set the role - default is RIDER if not specified
		if (userDTO.getRole() != null) {
			user.setRole(userDTO.getRole());
		}
		
		return userRepository.save(user);
	}

	@Override
	public User updateUser(Long id, UserDTO userDTO) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		if (userDTO.getFirstName() != null) {
			user.setFirstName(userDTO.getFirstName());
		}
		if (userDTO.getLastName() != null) {
			user.setLastName(userDTO.getLastName());
		}
		if (userDTO.getEmail() != null) {
			user.setEmail(userDTO.getEmail());
		}
		if (userDTO.getPhoneNumber() != null) {
			user.setPhoneNumber(userDTO.getPhoneNumber());
		}
		if (userDTO.getPassword() != null) {
			user.setPassword(userDTO.getPassword());
		}
		if (userDTO.getProfilePicture() != null) {
			user.setProfilePicture(userDTO.getProfilePicture());
		}
		if (userDTO.getAccountStatus() != null) {
			user.setAccountStatus(AccountStatus.valueOf(userDTO.getAccountStatus()));
		}
		if (userDTO.getIsVerified() != null) {
			user.setIsVerified(userDTO.getIsVerified());
		}
		if (userDTO.getRating() != null) {
			user.setRating(userDTO.getRating());
		}
		if (userDTO.getTotalTrips() != null) {
			user.setTotalTrips(userDTO.getTotalTrips());
		}
		if (userDTO.getRole() != null) {
			user.setRole(userDTO.getRole());
		}
			
		return userRepository.save(user);
	}

	@Override
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	@Override
	public UserDetailsDTO getUserDetailsById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		
		// Get the ranks managed by this user
		List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(id);
		
		return UserDetailsDTO.fromUser(user, managedRanks);
	}
	
	@Override
	public UserDetailsDTO getUserDetailsByIdWithRoleFilter(Long id, UserRole viewerRole) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		
		// Get the ranks managed by this user
		List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(id);
		
		return UserDetailsDTO.fromUserWithRoleFilter(user, managedRanks, viewerRole);
	}
} 