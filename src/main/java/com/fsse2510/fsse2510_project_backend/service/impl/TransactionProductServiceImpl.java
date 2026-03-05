package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.domainObject.response.TransactionProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import com.fsse2510.fsse2510_project_backend.mapper.transactionProduct.TransactionProductDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.transactionProduct.TransactionProductEntityMapper;
import com.fsse2510.fsse2510_project_backend.service.TransactionProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProductServiceImpl implements TransactionProductService {

    private final TransactionProductEntityMapper transactionProductEntityMapper;
    private final TransactionProductDataMapper transactionProductDataMapper;

    @Override
    public TransactionProductEntity createEntity(TransactionEntity transaction,
            CartItemResponseData cartItem,
            ProductResponseData productData) {

        TransactionProductResponseData snapshotData = transactionProductDataMapper
                .toData(cartItem, productData);

        TransactionProductEntity entity = transactionProductEntityMapper.toEntity(snapshotData, transaction);

        entity.setDescription(null);

        return entity;
    }

}
