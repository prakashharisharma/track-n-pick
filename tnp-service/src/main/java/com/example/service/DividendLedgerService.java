package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.DividendLedger;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.DividendLedgerRepository;
import com.example.repo.stocks.PortfolioRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class DividendLedgerService {

	@Autowired
	private DividendLedgerRepository dividendLedgerRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MiscUtil miscUtil;

	public void addDividend(UserProfile user, Stock stock, double perShareAmount, LocalDate exDate, LocalDate recordDate,
			LocalDate transactionDate) {
		long quantity = portfolioRepository.findByPortfolioIdUserAndPortfolioIdStock(user, stock).getQuantity();
		DividendLedger dl = new DividendLedger(user, stock, perShareAmount, quantity, exDate, recordDate,
				transactionDate);

		dividendLedgerRepository.save(dl);
	}

	public List<DividendLedger> recentDividends(UserProfile user) {
		return dividendLedgerRepository.findByUserId(user);
	}

	public double currentYearDividend(UserProfile user) {

		Double totalDividend = dividendLedgerRepository.getTotalDividendBetweenTwoDates(user,
				miscUtil.currentYearFirstDay(), miscUtil.currentDate());


		if (totalDividend == null) {
			totalDividend = 0.00;
		}
		
		return totalDividend;
	}

	public double currentFinYearDividend(UserProfile user) {

		Double totalDividend = dividendLedgerRepository.getTotalDividendBetweenTwoDates(user,
				miscUtil.currentFinYearFirstDay(), miscUtil.currentDate());

		if (totalDividend == null) {
			totalDividend = 0.00;
		}
		
		return totalDividend;
	}
}
