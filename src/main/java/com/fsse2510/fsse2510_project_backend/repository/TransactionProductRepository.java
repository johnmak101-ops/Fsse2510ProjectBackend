package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionProductRepository extends JpaRepository<TransactionProductEntity, Integer> {
}
