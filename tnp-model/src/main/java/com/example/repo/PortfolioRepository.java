package com.example.repo;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.stocks.Portfolio;
import com.example.model.um.User;

@Transactional
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	Portfolio findByUser(User user);
	
}
