package com.example.transactional.model.stocks;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.master.Stock;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "stock_technicals", indexes = {
        @Index(name = "idx_stock_timeframe", columnList = "STOCK_ID, TIMEFRAME"),
        @Index(name = "idx_session_date", columnList = "SESSION_DATE")
})
public abstract class StockTechnicals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long stockTechnicalsId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
    private Stock stock;

    @Column(name = "SMA_5", columnDefinition="decimal(20,2) default '0.00'")
    private Double sma5 = 0.00;

    @Column(name = "SMA_10", columnDefinition="decimal(20,2) default '0.00'")
    private Double sma10 = 0.00;

    @Column(name = "SMA_20", columnDefinition="decimal(20,2) default '0.00'")
    private Double sma20 = 0.00;

    @Column(name = "SMA_50", columnDefinition="decimal(20,2) default '0.00'")
    private Double sma50 = 0.00;

    @Column(name = "SMA_100", columnDefinition="decimal(20,2) default '0.00'")
    private Double sma100 = 0.00;

    @Column(name = "SMA_200", columnDefinition="decimal(20,2) default '0.00'")
    private Double sma200 = 0.00;

    @Column(name = "EMA_5", columnDefinition="decimal(20,2) default '0.00'")
    private Double ema5 = 0.00;

    @Column(name = "EMA_10", columnDefinition="decimal(20,2) default '0.00'")
    private Double ema10 = 0.00;

    @Column(name = "EMA_20", columnDefinition="decimal(20,2) default '0.00'")
    private Double ema20 = 0.00;

    @Column(name = "EMA_50", columnDefinition="decimal(20,2) default '0.00'")
    private Double ema50 = 0.00;

    @Column(name = "EMA_100", columnDefinition="decimal(20,2) default '0.00'")
    private Double ema100 = 0.00;

    @Column(name = "EMA_200", columnDefinition="decimal(20,2) default '0.00'")
    private Double ema200 = 0.00;

    @Column(name = "RSI", columnDefinition="decimal(20,2) default '0.00'")
    private Double rsi = 0.00;

    @Column(name = "MACD", columnDefinition="decimal(20,2) default '0.00'")
    private Double macd = 0.00;

    @Column(name = "SIGNAL_LINE", columnDefinition="decimal(20,2) default '0.00'")
    private Double signal = 0.00;

    @Column(name = "OBV", columnDefinition="bigint default '0'")
    private Long obv = 0L;

    @Column(name = "OBV_AVG", columnDefinition="bigint default '0'")
    private Long obvAvg = 0L;

    @Column(name = "VOLUME", columnDefinition="bigint default '0'")
    private Long volume = 0L;

    @Column(name = "VOLUME_AVG5", columnDefinition="bigint default '0'")
    private Long volumeAvg5 = 0L;

    @Column(name = "VOLUME_AVG10", columnDefinition="bigint default '0'")
    private Long volumeAvg10 = 0L;

    @Column(name = "VOLUME_AVG20", columnDefinition="bigint default '0'")
    private Long volumeAvg20 = 0L;

    @Column(name = "ADX", columnDefinition="decimal(20,2) default '0.00'")
    private Double adx = 0.00;

    @Column(name = "PLUS_DI", columnDefinition="decimal(20,2) default '0.00'")
    private Double plusDi = 0.00;

    @Column(name = "MINUS_DI", columnDefinition="decimal(20,2) default '0.00'")
    private Double minusDi = 0.00;

    @Column(name = "ATR", columnDefinition="decimal(20,2) default '0.00'")
    private Double atr = 0.00;
    
    // Previous Session Technicals
    @Column(name = "PREV_SMA_5", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevSma5 = 0.00;

    @Column(name = "PREV_SMA_10", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevSma10 = 0.00;

    @Column(name = "PREV_SMA_20", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevSma20 = 0.00;

    @Column(name = "PREV_SMA_50", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevSma50 = 0.00;

    @Column(name = "PREV_SMA_100", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevSma100 = 0.00;

    @Column(name = "PREV_SMA_200", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevSma200 = 0.00;

    @Column(name = "PREV_EMA_5", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevEma5 = 0.00;

    @Column(name = "PREV_EMA_10", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevEma10 = 0.00;

    @Column(name = "PREV_EMA_20", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevEma20 = 0.00;

    @Column(name = "PREV_EMA_50", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevEma50 = 0.00;

    @Column(name = "PREV_EMA_100", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevEma100 = 0.00;

    @Column(name = "PREV_EMA_200", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevEma200 = 0.00;

    @Column(name = "PREV_RSI", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevRsi = 0.00;

    @Column(name = "PREV_MACD", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevMacd = 0.00;

    @Column(name = "PREV_SIGNAL_LINE", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevSignal = 0.00;

    @Column(name = "PREV_OBV", columnDefinition="bigint default '0'")
    private Long prevObv = 0L;

    @Column(name = "PREV_OBV_AVG", columnDefinition="bigint default '0'")
    private Long prevObvAvg = 0L;

    @Column(name = "PREV_VOLUME", columnDefinition="bigint default '0'")
    private Long prevVolume = 0L;

    @Column(name = "PREV_VOLUME_AVG5", columnDefinition="bigint default '0'")
    private Long prevVolumeAvg5 = 0L;

    @Column(name = "PREV_VOLUME_AVG10", columnDefinition="bigint default '0'")
    private Long prevVolumeAvg10 = 0L;

    @Column(name = "PREV_VOLUME_AVG20", columnDefinition="bigint default '0'")
    private Long prevVolumeAvg20 = 0L;

    @Column(name = "PREV_ADX", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevAdx = 0.00;

    @Column(name = "PREV_PLUS_DI", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevPlusDi = 0.00;

    @Column(name = "PREV_MINUS_DI", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevMinusDi = 0.00;

    @Column(name = "PREV_ATR", columnDefinition="decimal(20,2) default '0.00'")
    private Double prevAtr = 0.00;

    // Previous 2 Session Technicals
    @Column(name = "PREV_2_SMA_5", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Sma5 = 0.00;

    @Column(name = "PREV_2_SMA_10", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Sma10 = 0.00;

    @Column(name = "PPREV_2_SMA_20", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Sma20 = 0.00;

    @Column(name = "PREV_2_SMA_50", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Sma50 = 0.00;

    @Column(name = "PREV_2_SMA_100", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Sma100 = 0.00;

    @Column(name = "PREV_2_SMA_200", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Sma200 = 0.00;

    @Column(name = "PREV_2_EMA_5", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Ema5 = 0.00;

    @Column(name = "PREV_2_EMA_10", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Ema10 = 0.00;

    @Column(name = "PPREV_2_EMA_20", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Ema20 = 0.00;

    @Column(name = "PREV_2_EMA_50", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Ema50 = 0.00;

    @Column(name = "PREV_2_EMA_100", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Ema100 = 0.00;

    @Column(name = "PREV_2_EMA_200", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Ema200 = 0.00;

    @Column(name = "PREV_2_RSI", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Rsi = 0.00;

    @Column(name = "PREV_2_MACD", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Macd = 0.00;

    @Column(name = "PREV_2_SIGNAL_LINE", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Signal = 0.00;

    @Column(name = "PREV_2_OBV", columnDefinition="bigint default '0'")
    private Long prev2Obv = 0L;

    @Column(name = "PREV_2_OBV_AVG", columnDefinition="bigint default '0'")
    private Long prev2ObvAvg = 0L;

    @Column(name = "PREV_2_VOLUME", columnDefinition="bigint default '0'")
    private Long prev2Volume = 0L;

    @Column(name = "PREV_2_VOLUME_AVG5", columnDefinition="bigint default '0'")
    private Long prev2VolumeAvg5 = 0L;

    @Column(name = "PREV_2_VOLUME_AVG10", columnDefinition="bigint default '0'")
    private Long prev2VolumeAvg10 = 0L;

    @Column(name = "PREV_2_VOLUME_AVG20", columnDefinition="bigint default '0'")
    private Long prev2VolumeAvg20 = 0L;

    @Column(name = "PREV_2_ADX", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Adx = 0.00;

    @Column(name = "PREV_2_PLUS_DI", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2PlusDi = 0.00;

    @Column(name = "PREV_2_MINUS_DI", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2MinusDi = 0.00;

    @Column(name = "PREV_2_ATR", columnDefinition="decimal(20,2) default '0.00'")
    private Double prev2Atr = 0.00;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIMEFRAME", nullable = false)
    private Timeframe timeframe = Timeframe.DAILY;

    @Column(name = "SESSION_DATE", nullable = false)
    private LocalDate sessionDate = LocalDate.now();

}
