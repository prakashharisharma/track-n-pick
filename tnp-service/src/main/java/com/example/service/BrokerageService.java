package com.example.service;

import javax.transaction.Transactional;

import com.example.data.transactional.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.data.transactional.entities.UserBrokerage;
import com.example.data.transactional.repo.UserBrokerageRepository;

@Transactional
@Service
public class BrokerageService {

	@Autowired
	private UserBrokerageRepository userBrokerageRepository;
	
	public UserBrokerage getBrokerage(User user) {
		
		return userBrokerageRepository.findByBrokerageIdUserAndActive(user, true);
	}
}
