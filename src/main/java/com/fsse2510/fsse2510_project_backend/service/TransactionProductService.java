package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;

public interface TransactionProductService {

    TransactionProductEntity createEntity(TransactionEntity transaction,
                                          CartItemResponseData cartItem,
                                          ProductResponseData productData);
}
