package com.example.repo.master;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Sector;

@Transactional
@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {
	
	Sector findBySectorId(long sectorId);
	
	List<Sector> findBySectorName(String sectorName);
	
	public List<Sector> findAll();
	
}
