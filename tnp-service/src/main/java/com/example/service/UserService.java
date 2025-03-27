package com.example.service;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import com.example.model.um.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.repo.um.UserRepository;

@Slf4j
@Service
@Transactional
public class UserService {
	
	@Autowired
	private UserRepository userRepository;


}
