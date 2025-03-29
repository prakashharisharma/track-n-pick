package com.example.service;

import javax.transaction.Transactional;

import com.example.transactional.model.um.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.transactional.model.um.UserBrokerage;
import com.example.transactional.repo.um.UserBrokerageRepository;

@Transactional
@Service
public class BrokerageService {

	@Autowired
	private UserBrokerageRepository userBrokerageRepository;
	
	public UserBrokerage getBrokerage(User user) {
		
		return userBrokerageRepository.findByBrokerageIdUserAndActive(user, true);
	}
}
