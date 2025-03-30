package com.example.transactional.model.subscription.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_subscription_details")
@Getter
@Setter
public class UserSubscriptionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private UserSubscriptionOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_code", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "discounted_price", columnDefinition = "Decimal(20,2) default '0.00'", nullable = false)
    private BigDecimal discountedPrice;
}
