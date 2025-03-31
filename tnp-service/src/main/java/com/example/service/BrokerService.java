package com.example.service;

import javax.transaction.Transactional;

import com.example.data.transactional.entities.Broker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.data.transactional.repo.BrokerRepository;

@Transactional
@Service
public class BrokerService {

	@Autowired
	private BrokerRepository brokerRepository;
	
	public Broker getBrokerById(long brokerId) {
		return brokerRepository.findByBrokerId(brokerId);
	}
}
