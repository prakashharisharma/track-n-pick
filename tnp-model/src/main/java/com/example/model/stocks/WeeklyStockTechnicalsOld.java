package com.example.model.stocks;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.*;

import com.example.model.master.Stock;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "WEEKLY_STOCK_TECHNICALS_OLD")
public class WeeklyStockTechnicalsOld implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -5870154333285682947L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STOCK_TECHNICALS_ID")
    long stockTechnicalsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
    Stock stock;

    @Column(name = "BHAV_DATE")
    LocalDate bhavDate = LocalDate.now();
    @Column(name = "LAST_MODIFIED")
    LocalDate lastModified = LocalDate.now();


    @Column(name = "EMA_5", columnDefinition="decimal(10,2) default '0.00'")
    double ema5;
    @Column(name = "PREV_EMA_5", columnDefinition="decimal(10,2) default '0.00'")
    double prevEma5;
    @Column(name = "PREV_PREV_EMA_5", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevEma5;


    @Column(name = "EMA_10", columnDefinition="decimal(10,2) default '0.00'")
    double ema10;
    @Column(name = "PREV_EMA_10", columnDefinition="decimal(10,2) default '0.00'")
    double prevEma10;
    @Column(name = "PREV_PREV_EMA_10", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevEma10;


    @Column(name = "EMA_20", columnDefinition="decimal(10,2) default '0.00'")
    double ema20;
    @Column(name = "PREV_EMA_20", columnDefinition="decimal(10,2) default '0.00'")
    double prevEma20;
    @Column(name = "PREV_PREV_EMA_20", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevEma20;


    @Column(name = "EMA_50", columnDefinition="decimal(10,2) default '0.00'")
    double ema50;
    @Column(name = "PREV_EMA_50", columnDefinition="decimal(10,2) default '0.00'")
    double prevEma50;
    @Column(name = "PREV_PREV_EMA_50", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevEma50;


    @Column(name = "EMA_100", columnDefinition="decimal(10,2) default '0.00'")
    double ema100;
    @Column(name = "PREV_EMA_100", columnDefinition="decimal(10,2) default '0.00'")
    double prevEma100;
    @Column(name = "PREV_PREV_EMA_100", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevEma100;


    @Column(name = "EMA_200", columnDefinition="decimal(10,2) default '0.00'")
    double ema200;
    @Column(name = "PREV_EMA_200", columnDefinition="decimal(10,2) default '0.00'")
    double prevEma200;
    @Column(name = "PREV_PREV_EMA_200", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevEma200;


    @Column(name = "RSI", columnDefinition="decimal(10,2) default '0.00'")
    double rsi;
    @Column(name = "PREV_RSI", columnDefinition="decimal(10,2) default '0.00'")
    double prevRsi;
    @Column(name = "PREV_PREV_RSI", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevRsi;


    @Column(name = "MACD", columnDefinition="decimal(10,2) default '0.00'")
    double macd;
    @Column(name = "PREV_MACD", columnDefinition="decimal(10,2) default '0.00'")
    double prevMacd;
    @Column(name = "PREV_PREV_MACD", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevMacd;


    @Column(name = "SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
    double signal;
    @Column(name = "PREV_SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
    double prevSignal;
    @Column(name = "PREV_PREV_SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevSignal;


    @Column(name = "OBV", columnDefinition="bigint default '0'")
    Long obv;
    @Column(name = "PREV_OBV", columnDefinition="bigint default '0'")
    Long prevObv;
    @Column(name = "PREV_PREV_OBV", columnDefinition="bigint default '0'")
    Long prevPrevObv;


    @Column(name = "OBV_AVG", columnDefinition="bigint default '0'")
    Long obvAvg;
    @Column(name = "PREV_OBV_AVG", columnDefinition="bigint default '0'")
    Long prevObvAvg;
    @Column(name = "PREV_PREV_OBV_AVG", columnDefinition="bigint default '0'")
    Long prevPrevObvAvg;


    @Column(name = "VOLUME", columnDefinition="bigint default '0'")
    Long volume;
    @Column(name = "PREV_VOLUME", columnDefinition="bigint default '0'")
    Long prevVolume;
    @Column(name = "PREV_PREV_VOLUME", columnDefinition="bigint default '0'")
    Long prevPrevVolume;


    @Column(name = "VOLUME_AVG5", columnDefinition="bigint default '0'")
    Long volumeAvg5;
    @Column(name = "PREV_VOLUME_AVG5", columnDefinition="bigint default '0'")
    Long prevVolumeAvg5;
    @Column(name = "PREV_PREV_VOLUME_AVG5", columnDefinition="bigint default '0'")
    Long prevPrevVolumeAvg5;


    @Column(name = "VOLUME_AVG20", columnDefinition="bigint default '0'")
    Long volumeAvg20;
    @Column(name = "PREV_VOLUME_AVG20", columnDefinition="bigint default '0'")
    Long prevVolumeAvg20;
    @Column(name = "PREV_PREV_VOLUME_AVG20", columnDefinition="bigint default '0'")
    Long prevPrevVolumeAvg20;


    @Column(name = "VOLUME_AVG50", columnDefinition="bigint default '0'")
    Long volumeAvg50;
    @Column(name = "PREV_VOLUME_AVG50", columnDefinition="bigint default '0'")
    Long prevVolumeAvg50;
    @Column(name = "PREV_PREV_VOLUME_AVG50", columnDefinition="bigint default '0'")
    Long prevPrevVolumeAvg50;


    @Column(name = "ADX", columnDefinition="decimal(10,2) default '0.00'")
    double adx;
    @Column(name = "PREV_ADX", columnDefinition="decimal(10,2) default '0.00'")
    double prevAdx;
    @Column(name = "PREV_PREV_ADX", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevAdx;


    @Column(name = "PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
    double plusDi;
    @Column(name = "PREV_PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
    double prevPlusDi;
    @Column(name = "PREV_PREV_PLUS_DI", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevPlusDi;


    @Column(name = "MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
    double minusDi;
    @Column(name = "PREV_MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
    double prevMinusDi;
    @Column(name = "PREV_PREV_MINUS_DI", columnDefinition="decimal(10,2) default '0.00'")
    double prevPrevMinusDi;


}
