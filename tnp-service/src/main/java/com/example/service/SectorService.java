package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Sector;
import com.example.repo.master.SectorRepository;

@Transactional
@Service
public class SectorService {

	@Autowired
	private SectorRepository sectorRepository;
	
	public Sector getSectorByName(String sectorName) {
		return sectorRepository.findBySectorNameContainingIgnoreCase(sectorName).get(0);
	}
	
	public List<Sector> allSectors(){
		return sectorRepository.findAll();
	}
	
	public Sector add(String sectorName, double sectorPe, double sectorPb) {
		
		Sector sector = new Sector(sectorName, sectorPe, sectorPb);
		
		return sectorRepository.save(sector);
	}
	
	public Sector update(String sectorName, double newSectorPe, double newSectorPb) {
		
		Sector sector = this.getSectorByName(sectorName);
		
		sector.setSectorPe(newSectorPe);
		sector.setSectorPb(newSectorPb);
		
		return sectorRepository.save(sector);
	}
	
	public boolean isExist(String sectorName) {
		Sector sector = this.getSectorByName(sectorName);
		
		if(sector != null) {
			return true;
		}else {
			return false;
		}
	}
}
