package com.example.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.UndervalueLedger;
import com.example.model.master.Stock;
import com.example.repo.ledger.UndervalueLedgerRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class UndervalueLedgerService {

	@Autowired
	private UndervalueLedgerRepository undervalueLedgerRepository;
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private MiscUtil miscUtil;
	
	public List<Stock> getCurrentUndervalueStocks(){
		
		List<UndervalueLedger> underValueLedgerList =  undervalueLedgerRepository.findByResearchDate(miscUtil.currentDate());
		
		return underValueLedgerList.stream().map( uvl -> uvl.getStockId()).collect(Collectors.toList());
	}
	
	public void add(Stock stock) {
		UndervalueLedger undervalueLedger = undervalueLedgerRepository.findByStockId(stock);
		
		if(undervalueLedger == null) {
			undervalueLedger = new UndervalueLedger();

			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);
			
			undervalueLedger.setStockId(stock);
			undervalueLedger.setPb(pb);
			undervalueLedger.setPe(pe);
			undervalueLedger.setResearchDate(LocalDate.now());

			undervalueLedgerRepository.save(undervalueLedger);
		}else {
			double newPe = stockService.getPe(stock);

			double newPb = stockService.getPb(stock);
			
			undervalueLedger.setNewPb(newPb);
			undervalueLedger.setNewPe(newPe);
			undervalueLedger.setLastModified(LocalDate.now());

			undervalueLedgerRepository.save(undervalueLedger);

		}
	}
	
	public void remove(Stock stock) {
		UndervalueLedger undervalueLedger = undervalueLedgerRepository.findByStockId(stock);
		
		if(undervalueLedger != null) {
			
			undervalueLedgerRepository.delete(undervalueLedger);
		}
	}
	
}
