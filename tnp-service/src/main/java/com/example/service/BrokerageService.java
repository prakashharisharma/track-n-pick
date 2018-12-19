package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.um.User;
import com.example.model.um.UserBrokerage;
import com.example.repo.um.UserBrokerageRepository;

@Transactional
@Service
public class BrokerageService {

	@Autowired
	private UserBrokerageRepository userBrokerageRepository;
	
	public UserBrokerage getBrokerage(User user) {
		
		return userBrokerageRepository.findByBrokerageIdUserAndActive(user, true);
	}
}
