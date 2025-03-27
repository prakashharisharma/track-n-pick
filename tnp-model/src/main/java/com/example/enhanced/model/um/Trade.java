package com.example.enhanced.model.um;

import com.example.model.master.Stock;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trades")
public class Trade {

    public enum Type {
        BUY, SELL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "STOCK_ID", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private Type type;

    @Column(name = "QUANTITY", columnDefinition="bigint default '0'")
    private Long quantity = Long.MIN_VALUE;

    @Column(name = "PRICE", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "STT", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal stt = BigDecimal.ZERO;

    @Column(name = "STAMP_DUTY", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal  stampDuty = BigDecimal.ZERO;

    @Column(name = "EXCHANGE_TXN_CHARGE", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal  exchangeTxnCharge = BigDecimal.ZERO;

    @Column(name = "SEBI_TURNOVER_FEE", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal  sebiTurnoverFee = BigDecimal.ZERO;

    @Column(name = "DP_CHARGE", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal  dpCharge = BigDecimal.ZERO;

    @Column(name = "BROKERAGE", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal brokerage = BigDecimal.ZERO;

    @Column(name = "GST", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal gst = BigDecimal.ZERO;

    @Column(name = "EFFECTIVE_PRICE", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal effectivePrice = BigDecimal.ZERO;

    @Column(name = "REALIZED_PNL", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal realizedPnl = BigDecimal.ZERO;

    @Column(name = "SESSION_DATE", nullable = false)
    private LocalDate sessionDate = LocalDate.now();

    @CreationTimestamp
    @Column(name = "CREATION_TIMESTAMP", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
