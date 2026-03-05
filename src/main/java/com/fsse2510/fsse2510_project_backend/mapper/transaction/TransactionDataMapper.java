package com.fsse2510.fsse2510_project_backend.mapper.transaction;

import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.mapper.transactionProduct.TransactionProductDataMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// [Important] uses = TransactionProductDataMapper.class
// This ensures MapStruct automatically uses the above Mapper to convert List<TransactionProductEntity> to List<Data>
@Mapper(componentModel = "spring", uses = { TransactionProductDataMapper.class })
public interface TransactionDataMapper {

    @Mapping(target = "buyerUid", source = "user.uid")
    @Mapping(target = "items", source = "items") // Automatically uses TransactionProductDataMapper
    @Mapping(target = "previousLevel", ignore = true)
    @Mapping(target = "newLevel", ignore = true)
    TransactionResponseData toData(TransactionEntity entity);
}
