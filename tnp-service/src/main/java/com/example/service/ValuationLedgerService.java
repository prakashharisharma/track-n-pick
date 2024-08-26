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

		ValuationLedger valuationLedger = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock,Type.UNDERVALUE, Status.OPEN);

		if (valuationLedger == null) {
			valuationLedger = new ValuationLedger();

			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);

			valuationLedger.setStockId(stock);
			valuationLedger.setPb(pb);
			valuationLedger.setPe(pe);
			valuationLedger.setResearchDate(LocalDate.now());

			valuationLedger.setType(Type.UNDERVALUE);

			valuationLedger.setStatus(Status.OPEN);
			
			valuationLedger.setPrice(stock.getStockPrice().getCurrentPrice());

			valuationLedger.setCurrentRatio(stock.getStockFactor().getCurrentRatio());
			
			valuationLedger.setDebtEquity(stock.getStockFactor().getDebtEquity());
			
			valuationLedger.setDividend(stock.getStockFactor().getDividend());
			
			valuationLedger.setSectorPb(stock.getSector().getSectorPb());
			
			valuationLedger.setSectorPe(stock.getSector().getSectorPe());

			valuationLedgerRepository.save(valuationLedger);
		}

		ValuationLedger valuationLedgerPrevOverValued = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock, Type.OVERVALUE,  Status.OPEN);

		if (valuationLedgerPrevOverValued != null) {
			valuationLedgerPrevOverValued.setStatus(Status.CLOSE);
			valuationLedgerPrevOverValued.setLastModified(LocalDate.now());
			valuationLedgerRepository.save(valuationLedgerPrevOverValued);
		}

		return valuationLedger;
		
	}

	public ValuationLedger addOvervalued(Stock stock) {
		ValuationLedger valuationLedger = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock,Type.OVERVALUE, Status.OPEN);

		if (valuationLedger == null) {
			valuationLedger = new ValuationLedger();

			double pe = stockService.getPe(stock);

			double pb = stockService.getPb(stock);

			valuationLedger.setStockId(stock);
			valuationLedger.setPb(pb);
			valuationLedger.setPe(pe);
			valuationLedger.setResearchDate(LocalDate.now());

			//undervalueLedger.setCategory(category);

			valuationLedger.setType(Type.OVERVALUE);

			valuationLedger.setStatus(Status.OPEN);
			
			
			valuationLedger.setPrice(stock.getStockPrice().getCurrentPrice());

			valuationLedger.setCurrentRatio(stock.getStockFactor().getCurrentRatio());
			
			valuationLedger.setDebtEquity(stock.getStockFactor().getDebtEquity());
			
			valuationLedger.setDividend(stock.getStockFactor().getDividend());
			
			valuationLedger.setSectorPb(stock.getSector().getSectorPb());
			
			valuationLedger.setSectorPe(stock.getSector().getSectorPe());
			

			valuationLedgerRepository.save(valuationLedger);
		}

		ValuationLedger valuationLedgerPrevUnderValued = valuationLedgerRepository.findByStockIdAndTypeAndStatus(stock, Type.UNDERVALUE, Status.OPEN);

		if (valuationLedgerPrevUnderValued != null) {
			valuationLedgerPrevUnderValued.setStatus(Status.CLOSE);
			valuationLedgerPrevUnderValued.setLastModified(LocalDate.now());
			valuationLedgerRepository.save(valuationLedgerPrevUnderValued);
		}
		
		return valuationLedger;
	}

}
