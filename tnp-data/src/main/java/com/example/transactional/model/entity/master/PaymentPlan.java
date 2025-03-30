package com.example.transactional.model.payment.entity;

import com.example.transactional.model.payment.type.PaymentPlanType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payment_plans")
public class PaymentPlan {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true)
    private PaymentPlanType code;

    @Column(name = "name", nullable = true, unique = false)
    private String name;

    @Column(name = "duration_months", columnDefinition = "int default 1", nullable = false)
    private int durationMonths;  // Renamed for clarity

    @Column(name = "discount_percentage", columnDefinition = "Decimal(20,2) default '0.00'", nullable = false)
    private BigDecimal discountPercentage;
}
