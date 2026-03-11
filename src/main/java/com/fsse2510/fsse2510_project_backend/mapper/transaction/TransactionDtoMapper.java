package com.fsse2510.fsse2510_project_backend.mapper.transaction;

import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.dto.response.TransactionResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.transactionProduct.TransactionProductDtoMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { TransactionProductDtoMapper.class })
public interface TransactionDtoMapper {

    TransactionResponseDto toDto(TransactionResponseData data);
}