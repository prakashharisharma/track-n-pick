package com.example.repo;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.um.User;

@Transactional
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByUserId(long userId);
	User findByUserEmail(String userEmail);
	
	
}