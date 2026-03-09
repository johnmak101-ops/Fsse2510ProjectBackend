package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.address.dto.request.CreateShippingAddressRequestDto;
import com.fsse2510.fsse2510_project_backend.data.address.dto.response.ShippingAddressResponseDto;

import java.util.List;

public interface ShippingAddressService {
    List<ShippingAddressResponseDto> getAllAddresses(String firebaseUid);

    ShippingAddressResponseDto createAddress(String firebaseUid, CreateShippingAddressRequestDto requestDto);

    ShippingAddressResponseDto updateAddress(String firebaseUid, Integer id, CreateShippingAddressRequestDto requestDto);

    void deleteAddress(String firebaseUid, Integer id);

    ShippingAddressResponseDto setDefaultAddress(String firebaseUid, Integer id);
}
