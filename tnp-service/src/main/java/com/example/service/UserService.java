package com.example.service;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.um.User;
import com.example.repo.UserRepository;

@Transactional
@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	public User save(User user) {
		return userRepository.save(user);
	}
	
	public User getUserById(long userId) {
		return userRepository.findByUserId(userId);
	}
	
	public User addtoWatchList(User user, List<Stock> stocksList) {
		
		Set<Stock> watchList = user.getWatchList();
		
		watchList.addAll(stocksList);
		
		user = userRepository.save(user);
		
		return user;
	}
	
}
