package com.example.transactional.model.subscription.entity;

import com.example.transactional.model.subscription.type.PaymentPlanType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private PaymentPlanType name;

    @Column(name = "duration", columnDefinition = "int default 1", nullable = false)
    private int durationMonths;

    @Column(name = "discount_percentage", columnDefinition = "Decimal(20,2) default '0.00'", nullable = false)
    private BigDecimal discountPercentage;
}
