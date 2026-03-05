package com.fsse2510.fsse2510_project_backend.mapper.payment;

import com.fsse2510.fsse2510_project_backend.data.payment.domainObject.response.PaymentResponseData;
import com.fsse2510.fsse2510_project_backend.data.payment.dto.response.PaymentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentDtoMapper {

    @Mapping(target = "tid", source = "transactionId")
    PaymentResponseDto toDto(PaymentResponseData data);
}
