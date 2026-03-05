package com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity;

import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tpid")
    private Integer tpid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tid", nullable = false)
    private TransactionEntity transaction;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "size")
    private String size;

    @Column(name = "color")
    private String color;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;
}
