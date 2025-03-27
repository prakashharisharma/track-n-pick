package com.example.enhanced.model.um;

import com.example.model.master.Stock;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "portfolio", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stock_id"}))
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "QUANTITY", columnDefinition="bigint default '0'")
    private Long quantity = Long.MIN_VALUE;

    @Column(name = "AVG_PRICE", precision = 20, scale = 6, columnDefinition = "DECIMAL(20,6) DEFAULT 0.000000")
    private BigDecimal avgPrice = BigDecimal.ZERO;
}
