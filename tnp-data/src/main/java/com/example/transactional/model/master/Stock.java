package com.example.transactional.model.master;

import java.io.Serializable;

import javax.persistence.*;

import com.example.model.type.Exchange;
import com.example.model.type.IndiceType;
import com.example.transactional.model.stocks.StockFactor;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "STOCK_MASTER",
		indexes = {
		@Index(name = "idx_company_name", columnList = "COMPANY_NAME"),
		@Index(name = "idx_nse_symbol", columnList = "NSE_SYMBOL")
}
)
public class Stock implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 291996847590635596L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STOCK_ID")
	long stockId;

	@Column(name = "SERIES")
	String series;

	@Column(name = "ISIN_CODE")
	String isinCode;
	
	@Column(name = "COMPANY_NAME")
	String companyName;
	
	@JsonProperty("symbol")
	@Column(name = "NSE_SYMBOL")
	String nseSymbol;
	
	@Column(name = "BSE_CODE")
	String bseCode;

	@Column(name = "SECTOR_NAME")
	String sectorName;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "sectorId", nullable = false)
	Sector sector;
	
	@Column(name = "IS_ACTIVE")
	private boolean active = true;

	@Column(name = "IS_ACTIVITY_COMPLETED")
	private boolean activityCompleted = false;

	@Column(name = "INDICE")
	@Enumerated(EnumType.STRING)
	private IndiceType primaryIndice;

	@Column(name = "EXCHANGE")
	@Enumerated(EnumType.STRING)
	private Exchange exchange;

	@OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
	private StockFactor factor;


	public Stock() {
		super();
		this.active = true;
		this.activityCompleted = false;
	}

	public Stock(String companyName, String nseSymbol, String bseCode) {
		super();
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.bseCode = bseCode;
		this.active = true;
		this.activityCompleted = false;
	}

	public Stock(Exchange exchange, String isinCode, String companyName, String nseSymbol, IndiceType primaryIndice, Sector sectorName) {
		super();
		this.exchange = exchange;
		this.isinCode = isinCode;
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.primaryIndice = primaryIndice;
		this.sector = sectorName;
		this.sectorName = sectorName.getSectorName();
		this.active = true;
		this.activityCompleted = false;
	}
	
	public Stock(String isinCode, String companyName, String nseSymbol, String sector) {
		super();
		this.isinCode = isinCode;
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.sectorName = sector;
		this.active = true;
		this.activityCompleted = false;
	}

	@Override
	public String toString() {
		return "Stock [stockId=" + stockId + ", isinCode=" + isinCode + ", companyName=" + companyName + ", nseSymbol="
				+ nseSymbol + ", bseCode=" + bseCode + ", sector=" + sectorName + ", active=" + active + ", IndiceType=" + primaryIndice + ", stockFactor=" + factor + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isinCode == null) ? 0 : isinCode.hashCode());
		result = prime * result + ((nseSymbol == null) ? 0 : nseSymbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if (isinCode == null) {
			if (other.isinCode != null)
				return false;
		} else if (!isinCode.equals(other.isinCode))
			return false;
		if (nseSymbol == null) {
			if (other.nseSymbol != null)
				return false;
		} else if (!nseSymbol.equals(other.nseSymbol))
			return false;
		return true;
	}
}
