package com.example.model.ledger;

import java.io.Serializable;
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

@Entity
@Table(name = "BREAKOUT_LEDGER")
public class BreakoutLedger implements Serializable{

	public enum BreakoutType {POSITIVE, NEGATIVE}

	/**
	 * - GAP_UP - Price open and close above previous close
	 * - MACD_CROSS_VOLUME_HIGH - MACD crossing over signal line and volume > avg_10
	 * - SIGNAL_NEAR_AND_ABOVE_ZERO_LINE - st.signal_line < (st.macd - st.signal_line)
	 * - VOLUME_HIGH - volume > avg_10
	 * - RULE 2 is true
	 */
	public enum BreakoutCategory { CROSS50, CROSS200, GAP_UP,  MACD_CROSS_VOLUME_HIGH, SIGNAL_NEAR_HISTOGRAM, VOLUME_HIGH, RULE2}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2127000992678133638L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "BREAKOUT_LEDGER_ID")
	long breakoutId;

	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "BREAKOUT_DATE")
	LocalDate breakoutDate = LocalDate.now();
	
	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	BreakoutType breakoutType;
	
	@Column(name = "CATEGORY")
	@Enumerated(EnumType.STRING)
	BreakoutCategory breakoutCategory;


	public BreakoutLedger() {
		super();
	}

	public BreakoutLedger(Stock stockId, LocalDate breakoutDate, BreakoutType breakoutType,
			BreakoutCategory breakoutCategory) {
		super();
		this.stockId = stockId;
		this.breakoutDate = breakoutDate;
		this.breakoutType = breakoutType;
		this.breakoutCategory = breakoutCategory;
	}

	public long getBreakoutId() {
		return breakoutId;
	}

	public void setBreakoutId(long breakoutId) {
		this.breakoutId = breakoutId;
	}

	public Stock getStockId() {
		return stockId;
	}

	public void setStockId(Stock stockId) {
		this.stockId = stockId;
	}

	public LocalDate getBreakoutDate() {
		return breakoutDate;
	}

	public void setBreakoutDate(LocalDate breakoutDate) {
		this.breakoutDate = breakoutDate;
	}

	public BreakoutType getBreakoutType() {
		return breakoutType;
	}

	public void setBreakoutType(BreakoutType breakoutType) {
		this.breakoutType = breakoutType;
	}

	public BreakoutCategory getBreakoutCategory() {
		return breakoutCategory;
	}

	public void setBreakoutCategory(BreakoutCategory breakoutCategory) {
		this.breakoutCategory = breakoutCategory;
	}

	@Override
	public String toString() {
		return "BreakoutLedger{" +
				"breakoutId=" + breakoutId +
				", stockId=" + stockId +
				", breakoutDate=" + breakoutDate +
				", breakoutType=" + breakoutType +
				", breakoutCategory=" + breakoutCategory +
				'}';
	}
}
