package com.example.transactional.model.research;

import javax.persistence.*;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.um.Trade;
import com.example.transactional.model.master.Stock;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;


@Getter
@Setter
@ToString
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "RESEARCH_TECHNICAL", indexes = {
		@Index(name = "idx_stock_timeframe", columnList = "STOCK_ID, TIMEFRAME"),
		@Index(name = "idx_research_date", columnList = "RESEARCH_DATE"),
		@Index(name = "idx_exit_date", columnList = "EXIT_DATE")
})
public class ResearchTechnical {

	/**
	 * BREAKOUT - Price Breakout with high volume
	 * SWING - Swing along with Moving Average
	 * PRICE_ACTION - Candle Stick
	 */
	public enum Strategy {
		PRICE, VOLUME, SWING,
	}

	public enum SubStrategy {
		SRTF,SRMA,  RMAO, HV, RM, TEMA
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long researchTechnicalsId;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	private Stock stock;
	
	@Column(name = "TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private Trade.Type type;

	@Column(name = "STRATEGY", nullable = false)
	@Enumerated(EnumType.STRING)
	private Strategy strategy;

	@Column(name = "SUB_STRATEGY", nullable = false)
	@Enumerated(EnumType.STRING)
	private SubStrategy subStrategy;

	@Column(name = "RESEARCH_DATE", nullable = false)
	private LocalDate researchDate = LocalDate.now();

	@Column(name = "EXIT_DATE")
	private LocalDate exitDate;

	@Column(name = "RESEARCH_PRICE", columnDefinition="Decimal(20,2) default '0.00'")
	private Double researchPrice;

	@Column(name = "EXIT_PRICE", columnDefinition="Decimal(20,2) default '0.00'")
	private Double exitPrice;

	@Column(name = "STOP_LOSS", columnDefinition="Decimal(20,2) default '0.00'")
	private Double stopLoss;

	@Column(name = "TARGET", columnDefinition="Decimal(20,2) default '0.00'")
	private Double target;

	@Column(name = "RISK", columnDefinition="Decimal(20,2) default '0.00'")
	private Double risk;


	@Column(name = "SCORE", columnDefinition="Decimal(20,2) default '0.00'")
	private Double score;

	@Enumerated(EnumType.STRING)
	@Column(name = "TIMEFRAME", nullable = false)
	private Timeframe timeframe = Timeframe.DAILY;

}
