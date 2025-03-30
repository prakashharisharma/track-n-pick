package com.example.transactional.model.entity.stock;

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
@Table(name = "stock_price", indexes = {
        @Index(name = "idx_stock_timeframe", columnList = "STOCK_ID, TIMEFRAME"),
        @Index(name = "idx_session_date", columnList = "SESSION_DATE")
})
public abstract class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STOCK_ID", referencedColumnName = "STOCK_ID", nullable = false)
    private Stock stock;

    @Column(name = "OPEN", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double open = 0.0;
    @Column(name = "HIGH", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double high = 0.0;
    @Column(name = "LOW", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double low = 0.0;
    @Column(name = "CLOSE", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double close = 0.0;

    // Previous Session OHLC
    @Column(name = "PREV_OPEN", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prevOpen = 0.0;
    @Column(name = "PREV_HIGH", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prevHigh = 0.0;
    @Column(name = "PREV_LOW", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prevLow = 0.0;
    @Column(name = "PREV_CLOSE", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prevClose = 0.0;

    // Previous 2 Session OHLC
    @Column(name = "PREV_2_OPEN", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev2Open = 0.0;
    @Column(name = "PREV_2_HIGH", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev2High = 0.0;
    @Column(name = "PREV_2_LOW", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev2Low = 0.0;
    @Column(name = "PREV_2_CLOSE", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev2Close = 0.0;

    // Previous 3 Session OHLC
    @Column(name = "PREV_3_OPEN", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev3Open = 0.0;
    @Column(name = "PREV_3_HIGH", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev3High = 0.0;
    @Column(name = "PREV_3_LOW", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev3Low = 0.0;
    @Column(name = "PREV_3_CLOSE", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev3Close = 0.0;

    // Previous 4 Session OHLC
    @Column(name = "PREV_4_OPEN", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev4Open = 0.0;
    @Column(name = "PREV_4_HIGH", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev4High = 0.0;
    @Column(name = "PREV_4_LOW", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev4Low = 0.0;
    @Column(name = "PREV_4_CLOSE", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double prev4Close = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIMEFRAME", nullable = false)
    private Timeframe timeframe = Timeframe.DAILY;

    @Column(name = "SESSION_DATE", nullable = false)
    private LocalDate sessionDate = LocalDate.now();

}
