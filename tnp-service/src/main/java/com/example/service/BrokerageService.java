package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.um.UserProfile;
import com.example.model.um.UserBrokerage;
import com.example.repo.um.UserBrokerageRepository;

@Transactional
@Service
public class BrokerageService {

	@Autowired
	private UserBrokerageRepository userBrokerageRepository;
	
	public UserBrokerage getBrokerage(UserProfile user) {
		
		return userBrokerageRepository.findByBrokerageIdUserAndActive(user, true);
	}
}
