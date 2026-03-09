package com.fsse2510.fsse2510_project_backend.data.transaction.entity;

import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tid")
    private Integer tid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_uid", nullable = false)
    private UserEntity user;

    @Column(name = "datetime", nullable = false)
    private LocalDateTime datetime;

    @Column(name = "status", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TransactionProductEntity> items = new ArrayList<>();

    @Column(name = "used_points")
    @Builder.Default
    private Integer usedPoints = 0;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "earned_points")
    @Builder.Default
    private BigDecimal earnedPoints = BigDecimal.ZERO;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    // Shipping Address Snapshot
    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state_province")
    private String stateProvince;

    @Column(name = "postal_code")
    private String postalCode;

    @Version
    private Integer version;
}