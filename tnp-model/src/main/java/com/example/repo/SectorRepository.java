package com.example.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Sector;


@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {
	
	Sector findBySectorId(long sectorId);
	
	List<Sector> findBySectorNameContainingIgnoreCase(String sectorName);
	
	public List<Sector> findAll();
	
}
