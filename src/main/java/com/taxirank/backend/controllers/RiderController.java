package com.taxirank.backend.controllers;

import java.net.http.HttpResponse.ResponseInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.taxirank.backend.dto.RiderDTO;
import com.taxirank.backend.models.Rider;
import com.taxirank.backend.services.RiderService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequestMapping("/api/riders")
public class RiderController {
	
	@Autowired
	private RiderService riderService;
	
	@GetMapping
	public ResponseEntity<List<Rider>> getAllRiders() {
		return ResponseEntity.ok(riderService.getAllRiders());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Rider> getRiderById(@PathVariable Long id) {
		return ResponseEntity.ok(riderService.getRiderById(id));
	}
	
	@PostMapping
	public ResponseEntity<Rider> createRider(@RequestBody RiderDTO riderDTO){
		return ResponseEntity.status(HttpStatus.CREATED).body(riderService.createRider(riderDTO));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Rider> updateRider(@PathVariable Long id, @RequestBody RiderDTO riderDTO) {
		return ResponseEntity.ok(riderService.updateRider(id, riderDTO));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteRider(@PathVariable Long id){
		riderService.deleteRider(id);
		return ResponseEntity.ok().build();
	}

}
