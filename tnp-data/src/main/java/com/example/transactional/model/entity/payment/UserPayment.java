package com.example.transactional.model.payment.entity;

import com.example.transactional.model.payment.type.PaymentFor;
import com.example.transactional.model.payment.type.PaymentMethod;
import com.example.transactional.model.um.User;
import com.example.transactional.model.payment.type.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_payments")
@Getter
@Setter
public class UserPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", columnDefinition = "Decimal(20,2) default '0.00'", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "reference_id")
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_for", nullable = false)
    private PaymentFor paymentFor;

}
