package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Sector;
import com.example.repo.SectorRepository;

@Transactional
@Service
public class SectorService {

	@Autowired
	private SectorRepository sectorRepository;
	
	public Sector getSectorByName(String sectorName) {
		return sectorRepository.findBySectorNameContainingIgnoreCase(sectorName).get(0);
	}
}
