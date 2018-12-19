package com.example.repo.stocks;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.stocks.UserPortfolioKey;
import com.example.model.um.User;

@Transactional
@Repository
public interface PortfolioRepository extends JpaRepository<UserPortfolio, UserPortfolioKey> {

	List<UserPortfolio>  findByPortfolioIdUser(User user);
	
	UserPortfolio  findByPortfolioIdUserAndPortfolioIdStock(User user, Stock stock);
	
	List<UserPortfolio> findAll();
	
}
