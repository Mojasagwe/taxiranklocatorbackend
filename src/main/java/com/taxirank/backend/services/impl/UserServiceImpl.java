package com.taxirank.backend.services.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taxirank.backend.dto.UserDTO;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.PaymentMethod;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;
	
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
		user.setPreferredPaymentMethod(PaymentMethod.valueOf(userDTO.getPreferredPaymentMethod()));
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
		if (userDTO.getPreferredPaymentMethod() != null) {
			user.setPreferredPaymentMethod(PaymentMethod.valueOf(userDTO.getPreferredPaymentMethod()));
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
} 