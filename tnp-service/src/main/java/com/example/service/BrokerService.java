package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.transactional.model.master.Broker;
import com.example.transactional.repo.master.BrokerRepository;

@Transactional
@Service
public class BrokerService {

	@Autowired
	private BrokerRepository brokerRepository;
	
	public Broker getBrokerById(long brokerId) {
		return brokerRepository.findByBrokerId(brokerId);
	}
}
