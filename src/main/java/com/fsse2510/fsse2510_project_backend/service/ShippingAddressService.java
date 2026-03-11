package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.address.domainObject.request.CreateShippingAddressRequestData;
import com.fsse2510.fsse2510_project_backend.data.address.domainObject.response.ShippingAddressResponseData;

import java.util.List;

public interface ShippingAddressService {
    List<ShippingAddressResponseData> getAllAddresses(String firebaseUid);

    ShippingAddressResponseData createAddress(String firebaseUid, CreateShippingAddressRequestData requestData);

    ShippingAddressResponseData updateAddress(String firebaseUid, Integer id, CreateShippingAddressRequestData requestData);

    void deleteAddress(String firebaseUid, Integer id);

    ShippingAddressResponseData setDefaultAddress(String firebaseUid, Integer id);
}
