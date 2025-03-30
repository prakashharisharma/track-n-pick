package com.example.transactional.model.subscription.entity;

import com.example.transactional.model.payment.entity.PaymentPlan;
import com.example.transactional.model.subscription.type.SubscriptionStatus;
import com.example.transactional.model.um.User;
import com.example.transactional.model.payment.entity.UserPayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_subscription_orders")
@Getter
@Setter
public class UserSubscriptionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_plan_code", nullable = false)
    private PaymentPlan paymentPlan;  // FIXED: Changed to ManyToOne

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", nullable = false)
    private UserPayment payment;

    @Column(name = "total_amount", columnDefinition = "Decimal(20,2) default '0.00'", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "auto_renewal", nullable = false)
    private boolean autoRenewal = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSubscriptionDetail> subscriptionDetails = new ArrayList<>(); // ADDED
}
