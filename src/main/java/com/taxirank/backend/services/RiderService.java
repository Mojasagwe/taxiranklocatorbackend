package com.taxirank.backend.services;

import java.util.List;
import com.taxirank.backend.dto.RiderDTO;
import com.taxirank.backend.models.Rider;

public interface RiderService {
	
	List<Rider> getAllRiders();
	Rider getRiderById(Long id);
	Rider createRider(RiderDTO riderDTO);
	Rider updateRider(Long id, RiderDTO riderDTO);
	void deleteRider(Long id);
	

}
