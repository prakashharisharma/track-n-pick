package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import com.example.data.transactional.entities.TaxMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.data.transactional.repo.TaxMasterRepository;

@Transactional
@Service
public class TaxMasterService {

	@Autowired
	private TaxMasterRepository taxMasterRepository;
	
	public TaxMaster getTaxMaster() {
		
		return taxMasterRepository.findByActive(true);
	}
	
	public List<TaxMaster> getTaxMaster1() {
		return taxMasterRepository.findAll();
	}
}
