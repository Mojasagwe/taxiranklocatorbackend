package com.taxirank.backend.services.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taxirank.backend.dto.RiderDTO;
import com.taxirank.backend.models.Rider;
import com.taxirank.backend.repositories.RankRepository;
import com.taxirank.backend.repositories.RiderRepository;
import com.taxirank.backend.services.RiderService;

@Service
@Transactional
public class RiderServiceImpl implements RiderService {
	
	@Autowired
	private RiderRepository riderRepository;
	
	@Autowired
	RankRepository rankRepository;
	
	@Override
	public List<Rider> getAllRiders() {
		return riderRepository.findAll();
	}
	
	@Override
	public Rider getRiderById(Long id) {
		return riderRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Rider not found with id: " + id));
	}
	
	@Override
	public Rider createRider(RiderDTO riderDTO) {
		Rider rider = new Rider();
		rider.setFirstName(riderDTO.getFirstName());
		rider.setLastName(riderDTO.getLastName());
		rider.setEmail(riderDTO.getEmail());
		rider.setPhoneNumber(riderDTO.getPhoneNumber());
		rider.setPassword(riderDTO.getPassword());
		rider.setProfilePicture(riderDTO.getProfilePicture());
		rider.setPreferredPaymentMethod(Rider.PaymentMethod.valueOf(riderDTO.getPreferredPaymentMethod()));
		rider.setAccountStatus(Rider.AccountStatus.valueOf(riderDTO.getAccountStatus()));
		rider.setIsVerified(riderDTO.getIsVerified());
		rider.setRating(riderDTO.getRating());
		rider.setTotalTrips(riderDTO.getTotalTrips());
		
		return riderRepository.save(rider);
	}

	@Override
	public Rider updateRider(Long id, RiderDTO riderDTO) {
		Rider rider = riderRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Rider Not found"));
			
		return riderRepository.save(rider);
	}

	@Override
	public void deleteRider(Long id) {
		riderRepository.deleteById(id);
	}
}
