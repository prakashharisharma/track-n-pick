package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.TaxMaster;
import com.example.repo.master.TaxMasterRepository;

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
