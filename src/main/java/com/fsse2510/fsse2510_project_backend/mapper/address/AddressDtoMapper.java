package com.fsse2510.fsse2510_project_backend.mapper.address;

import com.fsse2510.fsse2510_project_backend.data.address.domainObject.request.CreateShippingAddressRequestData;
import com.fsse2510.fsse2510_project_backend.data.address.domainObject.response.ShippingAddressResponseData;
import com.fsse2510.fsse2510_project_backend.data.address.dto.request.CreateShippingAddressRequestDto;
import com.fsse2510.fsse2510_project_backend.data.address.dto.response.ShippingAddressResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressDtoMapper {

    // Data -> DTO
    ShippingAddressResponseDto toResponseDto(ShippingAddressResponseData data);

    // DTO -> Data
    CreateShippingAddressRequestData toRequestData(CreateShippingAddressRequestDto dto);
}
