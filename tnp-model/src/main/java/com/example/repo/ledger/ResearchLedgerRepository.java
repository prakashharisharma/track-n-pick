package com.example.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ResearchLedger;
import com.example.model.ledger.ValuationLedger.Category;
import com.example.model.master.Stock;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;



@Transactional
@Repository
public interface ResearchLedgerRepository extends JpaRepository<ResearchLedger, Long> {

	ResearchLedger findBySrlId(long srlId); 
	
	ResearchLedger findByStockAndResearchTypeAndResearchStatus(Stock stock,ResearchType researchType, ResearchTrigger researchStatus);
	
	ResearchLedger findByStockAndResearchTypeAndResearchStatusAndNotifiedStorage(Stock stock,ResearchType researchType, ResearchTrigger researchStatus, boolean notifiedStorage);
	
	ResearchLedger findByStockAndResearchStatus(Stock stock, ResearchTrigger researchStatus);
	
	ResearchLedger findByStockAndResearchType(Stock stock, ResearchType researchType);
	
	ResearchLedger findByStockAndResearchTypeAndNotifiedBuy(Stock stock, ResearchType researchType,boolean isNotifiedBuy);
	
	ResearchLedger findByStockAndResearchTypeAndNotifiedSell(Stock stock, ResearchType researchType,boolean isNotifiedBuy);
	
	ResearchLedger findByStockAndResearchTypeAndCategory(Stock stock, ResearchType researchType, Category category);
	
	ResearchLedger findByStockAndResearchTypeAndNotifiedStorage(Stock stock, ResearchType researchType, boolean notifiedStorage);
	
	List<ResearchLedger> findByResearchStatus(ResearchTrigger researchStatus);
	
	List<ResearchLedger> findByResearchStatusAndCategory(ResearchTrigger researchStatus, Category category);
	
	List<ResearchLedger> findByResearchType(ResearchType researchType);
	
	List<ResearchLedger> findByResearchTypeAndResearchStatus(ResearchType researchType, ResearchTrigger researchStatus);

	List<ResearchLedger> findByResearchStatusAndNotifiedBuy(ResearchTrigger researchStatus,boolean isNotifiedBuy);
	
	List<ResearchLedger> findByResearchStatusAndNotifiedSell(ResearchTrigger researchStatus,boolean isNotifiedSell);
	
	List<ResearchLedger> findByNotifiedBuy(boolean isNotifiedBuy);
	
	List<ResearchLedger> findByNotifiedSell(boolean isNotifiedSell);
	
	List<ResearchLedger> findByNotifiedBuyOrNotifiedSell(boolean isNotifiedBuy, boolean isNotifiedSell);
	
	List<ResearchLedger> findAll();
	
}
