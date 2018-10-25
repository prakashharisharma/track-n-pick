package com.example.model.master;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SECTORS")
public class Sector {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SECTOR_ID")
	long sectorId;
	
	@Column(name = "SECTOR_NAME")
	String sectorName;

	@Override
	public String toString() {
		return "Sector [sectorId=" + sectorId + ", sectorName=" + sectorName + "]";
	}
	
	
}
