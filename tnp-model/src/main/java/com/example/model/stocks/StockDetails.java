package com.example.model.stocks;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;
import com.example.model.type.Quarter;

@Entity
@Table(name = "STOCK_DETAILS")
public class StockDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STOCK_DETAILS_ID")
	long stockDetailsId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "TOTAL_SHARES")
	long outstandingShares;
	
	@Column(name = "FINANCIAL_YEAR")
	LocalDate year;
	
	@Column(name = "QUARTER")
	@Enumerated(EnumType.STRING)
	Quarter  quarter;
	
	@Column(name = "FACE_VALUE")
	double faceValue;
	
	@Column(name = "SALES")
	double totalAnnualSales;
	
	@Column(name = "TOTAL_INCOME")
	double totalIncome;
	
	@Column(name = "NET_INCOME")
	double netIncome;
	
	@Column(name = "ASSETS")
	double totalAssets;
	
	@Column(name = "LIABILITIES")
	double totalLiabilities;
	
	@Column(name = "INVENTORY_COST")
	double totalInventoryCost;
	
	@Column(name = "INTEREST_EXPENSE")
	double totalInterestExpense;
	
	@Column(name = "BOOK_VALUE")
	double bookValue;
	
	@Column(name = "EPS")
	double earningPerShare;
	
	@Column(name = "DIVIDEND")
	double dividendPerShare;

	@Override
	public String toString() {
		return "StockDetails [stockDetailsId=" + stockDetailsId + ", stockId=" + stockId + ", outstandingShares="
				+ outstandingShares + ", year=" + year + ", faceValue=" + faceValue + ", totalAnnualSales="
				+ totalAnnualSales + ", totalIncome=" + totalIncome + ", netIncome=" + netIncome + ", totalAssets="
				+ totalAssets + ", totalLiabilities=" + totalLiabilities + ", totalInventoryCost=" + totalInventoryCost
				+ ", totalInterestExpense=" + totalInterestExpense + ", bookValue=" + bookValue + ", earningPerShare="
				+ earningPerShare + ", dividendPerShare=" + dividendPerShare + "]";
	}
	
	
	
}
