package com.example.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;

@Transactional
@Repository
public interface ResearchLedgerTechnicalRepository extends JpaRepository<ResearchLedgerTechnical, Long> {

	ResearchLedgerTechnical findBySrlId(long srlId); 
	
	ResearchLedgerTechnical findByStockAndResearchStatus(Stock stock, ResearchTrigger researchStatus);

	ResearchLedgerTechnical findByStock(Stock stock);
	
	ResearchLedgerTechnical findByStockAndNotified(Stock stock, boolean isNotified);
	
	//ResearchLedgerTechnical findByStockAndNotifiedStorage(Stock stock, ResearchType researchType, boolean notifiedStorage);
	
	//ResearchLedgerTechnical findByStockAndResearchStatusAndNotifiedStorage(Stock stock,  ResearchTrigger researchStatus, boolean notifiedStorage);
	
	List<ResearchLedgerTechnical> findByResearchStatus(ResearchTrigger researchStatus);

	List<ResearchLedgerTechnical> findByNotified(boolean isNotified);
	
	List<ResearchLedgerTechnical> findAll();
	
	List<ResearchLedgerTechnical> findByResearchStatusAndNotified(ResearchTrigger researchStatus,boolean isNotified);

}
