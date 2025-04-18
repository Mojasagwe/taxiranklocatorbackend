package com.taxirank.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;
import com.taxirank.backend.models.TaxiRank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private String profilePicture;
    private AccountStatus accountStatus;
    private Boolean isVerified;
    private Double rating;
    private Integer totalTrips;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRole role;
    private List<ManagedRankDTO> managedRanks = new ArrayList<>();
    
    public static UserDetailsDTO fromUser(User user, List<TaxiRank> managedRanks) {
        UserDetailsDTO dto = new UserDetailsDTO();
        
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPassword(null);
        dto.setProfilePicture(user.getProfilePicture());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setIsVerified(user.getIsVerified());
        dto.setRating(user.getRating());
        dto.setTotalTrips(user.getTotalTrips());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRole(user.getRole());
        
        // Convert managed ranks to DTOs
        if (managedRanks != null) {
            List<ManagedRankDTO> rankDTOs = managedRanks.stream()
                .map(rank -> {
                    ManagedRankDTO rankDTO = new ManagedRankDTO();
                    rankDTO.setId(rank.getId());
                    rankDTO.setName(rank.getName());
                    rankDTO.setCode(rank.getCode());
                    rankDTO.setCity(rank.getCity());
                    return rankDTO;
                })
                .collect(Collectors.toList());
            
            dto.setManagedRanks(rankDTOs);
        }
        
        return dto;
    }
    
    /**
     * Creates a UserDetailsDTO with fields filtered based on the viewer's role.
     * 
     * @param user The user entity
     * @param managedRanks List of taxi ranks managed by the user
     * @param viewerRole The role of the user viewing this data
     * @return A filtered UserDetailsDTO
     */
    public static UserDetailsDTO fromUserWithRoleFilter(User user, List<TaxiRank> managedRanks, UserRole viewerRole) {
        UserDetailsDTO dto = new UserDetailsDTO();
        
        // Common fields for all roles
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRole(user.getRole());
        
        // Never include password for any role
        dto.setPassword(null);
        
        // Role-specific fields
        switch (viewerRole) {
            case RIDER:
                // Commuter will have all fields except: managedRanks
                dto.setAccountStatus(user.getAccountStatus());
                dto.setIsVerified(user.getIsVerified());
                dto.setRating(user.getRating());
                dto.setTotalTrips(user.getTotalTrips());
                // managedRanks excluded
                break;
                
            case SUPER_ADMIN:
                // Super admin will have everything except: rating, totalTrips, isVerified, managedRanks, accountStatus
                // Explicitly set these fields to null to ensure they're not included
                dto.setRating(null);
                dto.setTotalTrips(null);
                dto.setIsVerified(null);
                dto.setAccountStatus(null);
                dto.setManagedRanks(null);
                break;
                
            case ADMIN:
                // Rank admin will have everything except: totalTrips
                dto.setAccountStatus(user.getAccountStatus());
                dto.setIsVerified(user.getIsVerified());
                dto.setRating(user.getRating());
                // totalTrips excluded
                
                // Add managed ranks for admin
                if (managedRanks != null) {
                    List<ManagedRankDTO> rankDTOs = managedRanks.stream()
                        .map(rank -> {
                            ManagedRankDTO rankDTO = new ManagedRankDTO();
                            rankDTO.setId(rank.getId());
                            rankDTO.setName(rank.getName());
                            rankDTO.setCode(rank.getCode());
                            rankDTO.setCity(rank.getCity());
                            return rankDTO;
                        })
                        .collect(Collectors.toList());
                    
                    dto.setManagedRanks(rankDTOs);
                }
                break;
                
            default:
                // Default case: return all fields except password
                dto = fromUser(user, managedRanks);
                dto.setPassword(null);
        }
        
        return dto;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(Integer totalTrips) {
        this.totalTrips = totalTrips;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<ManagedRankDTO> getManagedRanks() {
        return managedRanks;
    }

    public void setManagedRanks(List<ManagedRankDTO> managedRanks) {
        this.managedRanks = managedRanks;
    }
} 