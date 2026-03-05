package com.fsse2510.fsse2510_project_backend.mapper.transactionProduct;

import com.fsse2510.fsse2510_project_backend.data.transactionProduct.domainObject.response.TransactionProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.dto.response.TransactionProductResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionProductDtoMapper {
    TransactionProductResponseDto toDto(TransactionProductResponseData data);
}