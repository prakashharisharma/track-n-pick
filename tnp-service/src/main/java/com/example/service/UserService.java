package com.example.service;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.um.User;
import com.example.repo.UserRepository;

@Transactional
@Service
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserRepository userRepository;
	
	public List<User> activeUsers(){
		return userRepository.findByActive(true);
	}
	
	public List<User> allUsers(){
		return userRepository.findAll();
	}
	
	public User save(User user) {
		return userRepository.save(user);
	}
	
	public User getUserById(long userId) {
		return userRepository.findByUserId(userId);
	}
	
	public User addtoWatchList(User user, List<Stock> stocksList) {
		LOGGER.info("ADDING TO WATCHLIST...");
		Set<Stock> watchList = user.getWatchList();
		
		watchList.addAll(stocksList);
		
		user = userRepository.save(user);
		
		return user;
	}
	
	public void removeFromtoWatchList(User user, List<Stock> stocksList) {
		
		Set<Stock> watchList = user.getWatchList();
		
		watchList.removeAll(stocksList);
		
		user = userRepository.save(user);
		
	}
}
