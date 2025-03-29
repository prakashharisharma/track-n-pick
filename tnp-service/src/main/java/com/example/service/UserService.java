package com.example.service;

import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.transactional.repo.um.UserRepository;

@Slf4j
@Service
@Transactional
public class UserService {
	
	@Autowired
	private UserRepository userRepository;


}
