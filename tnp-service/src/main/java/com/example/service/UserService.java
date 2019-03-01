package com.example.service;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.repo.um.UserRepository;

@Transactional
@Service
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserRepository userRepository;
	
	public List<UserProfile> activeUsers(){
		return userRepository.findByActive(true);
	}
	
	public List<UserProfile> allUsers(){
		return userRepository.findAll();
	}
	
	public UserProfile save(UserProfile user) {
		return userRepository.save(user);
	}
	
	public UserProfile getUserById(long userId) {
		return userRepository.findByUserId(userId);
	}
	
	public UserProfile getUserByEmail(String userEmail) {
		return userRepository.findByUserEmail(userEmail);
	}
	public UserProfile getUserByUsername(String userName) {
		return userRepository.findByUserName(userName);
	}
	
	
	public UserProfile addtoWatchList(UserProfile user, List<Stock> stocksList) {
		LOGGER.info("ADDING TO WATCHLIST...");
		Set<Stock> watchList = user.getWatchList();
		LOGGER.info("UPDATING TECHNICALS BEFORE ADDING TO WATCHLIST...");
		stocksList.forEach(stock -> {
			//stockService.updateTechnicals(stock);
		});
		
		watchList.addAll(stocksList);
		
		user = userRepository.save(user);
		
		return user;
	}
	
	public void removeFromtoWatchList(UserProfile user, List<Stock> stocksList) {
		
		Set<Stock> watchList = user.getWatchList();
		
		watchList.removeAll(stocksList);
		
		user = userRepository.save(user);
		
	}
}
