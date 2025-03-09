package com.taxirank.backend.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ranks")
@Data
public class Rank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String location;
    
    private Double latitude;
    
    private Double longitude;
    
    @Column(name = "contact_number")
    private String contactNumber;
    
    @Column(name = "operating_hours")
    private String operatingHours;
    
    private String status;  // e.g., "ACTIVE", "INACTIVE"
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

	public void setCapacity(int i) {
		// TODO Auto-generated method stub
		
	}

	public Integer getCapacity() {
		// TODO Auto-generated method stub
		return null;
	}
}