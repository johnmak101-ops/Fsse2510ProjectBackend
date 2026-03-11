package com.fsse2510.fsse2510_project_backend.mapper.address;

import com.fsse2510.fsse2510_project_backend.data.address.domainObject.request.CreateShippingAddressRequestData;
import com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressEntityMapper {

    // Data -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "recipientName", source = "data.recipientName")
    @Mapping(target = "phoneNumber", source = "data.phoneNumber")
    @Mapping(target = "addressLine1", source = "data.addressLine1")
    @Mapping(target = "addressLine2", source = "data.addressLine2")
    @Mapping(target = "city", source = "data.city")
    @Mapping(target = "stateProvince", source = "data.stateProvince")
    @Mapping(target = "postalCode", source = "data.postalCode")
    @Mapping(target = "isDefault", source = "data.isDefault")
    ShippingAddressEntity toEntity(CreateShippingAddressRequestData data, UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "recipientName", source = "data.recipientName")
    @Mapping(target = "phoneNumber", source = "data.phoneNumber")
    @Mapping(target = "addressLine1", source = "data.addressLine1")
    @Mapping(target = "addressLine2", source = "data.addressLine2")
    @Mapping(target = "city", source = "data.city")
    @Mapping(target = "stateProvince", source = "data.stateProvince")
    @Mapping(target = "postalCode", source = "data.postalCode")
    @Mapping(target = "isDefault", source = "data.isDefault")
    void updateEntity(CreateShippingAddressRequestData data, @MappingTarget ShippingAddressEntity entity);
}
