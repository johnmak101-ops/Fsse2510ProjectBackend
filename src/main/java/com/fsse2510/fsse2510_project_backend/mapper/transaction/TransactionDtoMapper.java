package com.fsse2510.fsse2510_project_backend.mapper.transaction;

import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.request.CreateTransactionRequestData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.dto.request.CreateTransactionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.transaction.dto.response.TransactionResponseDto;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.mapper.transactionProduct.TransactionProductDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { TransactionProductDtoMapper.class })
public interface TransactionDtoMapper {

    // [New] Multi-source Mapping
    // source="user" refers to the 'user' parameter
    // source="dto.couponCode" refers to 'couponCode' within the 'dto' parameter
    @Mapping(target = "user", source = "user")
    @Mapping(target = "couponCode", source = "dto.couponCode")
    @Mapping(target = "usePoints", source = "dto.usePoints")
    @Mapping(target = "addressId", source = "dto.addressId")
    CreateTransactionRequestData toRequestData(CreateTransactionRequestDto dto, FirebaseUserData user);

    // Response (Unchanged)

    TransactionResponseDto toDto(TransactionResponseData data);
}