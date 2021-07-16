package com.example.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ValuationLedger;
import com.example.model.ledger.ValuationLedger.Status;
import com.example.model.ledger.ValuationLedger.Type;
import com.example.model.master.Stock;
import com.example.repo.ledger.ValuationLedgerRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class ValuationLedgerService {

	@Autowired
	private ValuationLedgerRepository valuationLedgerRepository;

	@Autowired
	private StockService stockService;

	@Autowired
	private MiscUtil miscUtil;

	public List<Stock> getCurrentUndervalueStocks() {

		List<ValuationLedger> underValueLedgerList = valuationLedgerRepository
				.findByResearchDate(miscUtil.currentDate());

		return underValueLedgerList.stream().map(uvl -> uvl.getStockId()).collect(Collectors.toList());
	}

	public ValuationLedger addUndervalued(Stock stock) {

		ValuationLedger undervalueLedger = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock,Type.UNDERVALUE, Status.OPEN);

		if (undervalueLedger == null) {
			undervalueLedger = new ValuationLedger();

			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);

			undervalueLedger.setStockId(stock);
			undervalueLedger.setPb(pb);
			undervalueLedger.setPe(pe);
			undervalueLedger.setResearchDate(LocalDate.now());

			//undervalueLedger.setCategory(category);

			undervalueLedger.setType(Type.UNDERVALUE);

			undervalueLedger.setStatus(Status.OPEN);
			
			undervalueLedger.setPrice(stock.getStockPrice().getCurrentPrice());

			undervalueLedger.setCurrentRatio(stock.getStockFactor().getCurrentRatio());
			
			undervalueLedger.setDebtEquity(stock.getStockFactor().getDebtEquity());
			
			undervalueLedger.setDividend(stock.getStockFactor().getDividend());
			
			undervalueLedger.setSectorPb(stock.getSector().getSectorPb());
			
			undervalueLedger.setSectorPe(stock.getSector().getSectorPe());
			
			
			valuationLedgerRepository.save(undervalueLedger);
		} /*else {
			double newPe = stockService.getPe(stock);

			double newPb = stockService.getPb(stock);

			undervalueLedger.setNewPb(newPb);
			undervalueLedger.setNewPe(newPe);
			undervalueLedger.setLastModified(LocalDate.now());

			valuationLedgerRepository.save(undervalueLedger);

		}*/

		ValuationLedger valuationLedgerPrevOverValued = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock, Type.OVERVALUE,  Status.OPEN);

		if (valuationLedgerPrevOverValued != null) {
			valuationLedgerPrevOverValued.setStatus(Status.CLOSE);
			valuationLedgerPrevOverValued.setLastModified(LocalDate.now());
			valuationLedgerRepository.save(valuationLedgerPrevOverValued);
		}

		return undervalueLedger;
		
	}

	public ValuationLedger addOvervalued(Stock stock) {
		ValuationLedger undervalueLedger = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock,Type.OVERVALUE, Status.OPEN);

		if (undervalueLedger == null) {
			undervalueLedger = new ValuationLedger();

			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);

			undervalueLedger.setStockId(stock);
			undervalueLedger.setPb(pb);
			undervalueLedger.setPe(pe);
			undervalueLedger.setResearchDate(LocalDate.now());

			//undervalueLedger.setCategory(category);

			undervalueLedger.setType(Type.OVERVALUE);

			undervalueLedger.setStatus(Status.OPEN);
			
			
			undervalueLedger.setPrice(stock.getStockPrice().getCurrentPrice());

			undervalueLedger.setCurrentRatio(stock.getStockFactor().getCurrentRatio());
			
			undervalueLedger.setDebtEquity(stock.getStockFactor().getDebtEquity());
			
			undervalueLedger.setDividend(stock.getStockFactor().getDividend());
			
			undervalueLedger.setSectorPb(stock.getSector().getSectorPb());
			
			undervalueLedger.setSectorPe(stock.getSector().getSectorPe());
			

			valuationLedgerRepository.save(undervalueLedger);
		} /*else {
			double newPe = stockService.getPe(stock);

			double newPb = stockService.getPb(stock);

			undervalueLedger.setNewPb(newPb);
			undervalueLedger.setNewPe(newPe);
			undervalueLedger.setLastModified(LocalDate.now());

			valuationLedgerRepository.save(undervalueLedger);

		}*/

		ValuationLedger valuationLedgerPrevUnderValued = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock, Type.UNDERVALUE, Status.OPEN);

		if (valuationLedgerPrevUnderValued != null) {
			valuationLedgerPrevUnderValued.setStatus(Status.CLOSE);
			valuationLedgerPrevUnderValued.setLastModified(LocalDate.now());
			valuationLedgerRepository.save(valuationLedgerPrevUnderValued);
		}
		
		return undervalueLedger;
	}

}
