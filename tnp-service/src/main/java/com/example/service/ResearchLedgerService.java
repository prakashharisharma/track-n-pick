package com.example.service;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;

import com.example.repo.ResearchLedgerRepository;
import com.example.util.Rules;

@Transactional
@Service
public class ResearchLedgerService {

	@Autowired
	private ResearchLedgerRepository researchLedgerRepository;

	@Autowired
	private Rules rules;

	public void addStock(Stock stock) {

		ResearchLedger ledgerStock = researchLedgerRepository.findByStockAndActive(stock, true);

		if (ledgerStock != null) {

			researchLedgerRepository.save(ledgerStock);
			
		} else {
			ledgerStock = new ResearchLedger();
			ledgerStock.setActive(true);
			ledgerStock.setResearchDate(LocalDate.now());
			ledgerStock.setResearchPrice(stock.getStockPrice().getCurrentPrice());
			ledgerStock.setStock(stock);

			double currentPrice = stock.getStockPrice().getCurrentPrice();

			double targetPrice = currentPrice + (currentPrice * rules.getTargetPer());

			ledgerStock.setTargetPrice(targetPrice);

			researchLedgerRepository.save(ledgerStock);
		}

	}
	
	public ResearchLedger ledgerDetails(Stock stock) {
		return researchLedgerRepository.findByStockAndActive(stock, true);
	}

	public boolean isActive(Stock stock) {

		boolean result = false;

		ResearchLedger ledgerStock = researchLedgerRepository.findByStockAndActive(stock, true);

		if (ledgerStock != null) {
			result = true;
		}

		return result;
	}
	
	public boolean includeInMail(Stock stock) {

		boolean result = true;

		ResearchLedger ledgerStock = researchLedgerRepository.findByStockAndActive(stock, true);

		if (ledgerStock != null) {
			result = false;
		}

		return result;
	}


	public void targetAchived(Stock stock) {
		ResearchLedger ledgerStock = researchLedgerRepository.findByStockAndActive(stock, true);
		
		if(ledgerStock!=null) {
		
			ledgerStock.setActive(false);
			ledgerStock.setTargetDate(LocalDate.now());
		}
	}
	
}
