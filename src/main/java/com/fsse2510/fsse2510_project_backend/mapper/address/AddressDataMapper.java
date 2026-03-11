package com.fsse2510.fsse2510_project_backend.mapper.address;

import com.fsse2510.fsse2510_project_backend.data.address.domainObject.response.ShippingAddressResponseData;
import com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressDataMapper {

    // Entity -> Data
    ShippingAddressResponseData toResponseData(ShippingAddressEntity entity);
}
