package com.example.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.um.UserPortfolioKey;

@Transactional
@Repository
public interface ResearchLedgerRepository extends JpaRepository<ResearchLedger, UserPortfolioKey> {

	ResearchLedger findByStockAndActive(Stock stock, boolean isActive);
	
	List<ResearchLedger> findAll();
	
}
