package com.example.transactional.model.subscription.entity;


import com.example.transactional.model.subscription.type.SubscriptionType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
public class SubscriptionPlan {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true)
    private SubscriptionType code;

    @Column(name = "name", nullable = true, unique = false)
    private String name;

    @Column(name = "base_price", columnDefinition = "Decimal(20,2) default '0.00'", nullable = false)
    private BigDecimal basePrice;
}
