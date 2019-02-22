package com.example.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.type.ResearchStatus;
import com.example.util.io.model.ResearchType;


@Transactional
@Repository
public interface ResearchLedgerRepository extends JpaRepository<ResearchLedger, Long> {

	ResearchLedger findByStockAndResearchTypeAndResearchStatus(Stock stock,ResearchType researchType, ResearchStatus researchStatus);
	
	ResearchLedger findByStockAndResearchStatus(Stock stock, ResearchStatus researchStatus);
	
	ResearchLedger findByStockAndResearchType(Stock stock, ResearchType researchType);
	
	List<ResearchLedger> findByResearchStatus(ResearchStatus researchStatus);
	
	List<ResearchLedger> findByResearchType(ResearchType researchType);
	
	List<ResearchLedger> findByResearchTypeAndResearchStatus(ResearchType researchType, ResearchStatus researchStatus);

	List<ResearchLedger> findByResearchStatusAndNotifiedBuy(ResearchStatus researchStatus,boolean isNotifiedBuy);
	
	List<ResearchLedger> findByResearchStatusAndNotifiedSell(ResearchStatus researchStatus,boolean isNotifiedSell);
	
	List<ResearchLedger> findByNotifiedBuy(boolean isNotifiedBuy);
	
	List<ResearchLedger> findByNotifiedSell(boolean isNotifiedSell);
	
	List<ResearchLedger> findByNotifiedBuyOrNotifiedSell(boolean isNotifiedBuy, boolean isNotifiedSell);
	
	List<ResearchLedger> findAll();
	
}
