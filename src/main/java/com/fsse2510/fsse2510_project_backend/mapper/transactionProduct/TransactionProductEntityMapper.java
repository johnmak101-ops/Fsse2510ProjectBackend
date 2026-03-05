package com.fsse2510.fsse2510_project_backend.mapper.transactionProduct;

import com.fsse2510.fsse2510_project_backend.data.transactionProduct.domainObject.response.TransactionProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionProductEntityMapper {

    @Mapping(target = "tpid", ignore = true)
    @Mapping(target = "transaction", source = "transaction")
    // All other fields map automatically by name
    TransactionProductEntity toEntity(TransactionProductResponseData data, TransactionEntity transaction);
}
