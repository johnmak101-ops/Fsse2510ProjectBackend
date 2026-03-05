package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.address.dto.request.CreateShippingAddressRequestDto;
import com.fsse2510.fsse2510_project_backend.data.address.dto.response.ShippingAddressResponseDto;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;

import java.util.List;

public interface ShippingAddressService {
    List<ShippingAddressResponseDto> getAllAddresses(UserEntity user);

    ShippingAddressResponseDto createAddress(UserEntity user, CreateShippingAddressRequestDto requestDto);

    ShippingAddressResponseDto updateAddress(UserEntity user, Integer id, CreateShippingAddressRequestDto requestDto);

    void deleteAddress(UserEntity user, Integer id);

    ShippingAddressResponseDto setDefaultAddress(UserEntity user, Integer id);
}
