package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.address.domainObject.request.CreateShippingAddressRequestData;
import com.fsse2510.fsse2510_project_backend.data.address.domainObject.response.ShippingAddressResponseData;
import com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.exception.address.AddressNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.address.AddressDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.address.AddressEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.ShippingAddressRepository;
import com.fsse2510.fsse2510_project_backend.service.ShippingAddressService;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {
    private final ShippingAddressRepository addressRepository;
    private final UserService userService;
    private final AddressEntityMapper addressEntityMapper;
    private final AddressDataMapper addressDataMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressResponseData> getAllAddresses(String firebaseUid) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);
        return addressRepository.findByUser(user).stream()
                .map(addressDataMapper::toResponseData)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingAddressResponseData createAddress(String firebaseUid, CreateShippingAddressRequestData requestData) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);

        if (Boolean.TRUE.equals(requestData.getIsDefault())) {
            unsetCurrentDefault(user);
        }

        ShippingAddressEntity entity = addressEntityMapper.toEntity(requestData, user);
        return addressDataMapper.toResponseData(addressRepository.save(entity));
    }

    @Override
    @Transactional
    public ShippingAddressResponseData updateAddress(String firebaseUid, Integer id,
            CreateShippingAddressRequestData requestData) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);

        ShippingAddressEntity entity = addressRepository.findById(id)
                .filter(a -> a.getUser().getUid().equals(user.getUid()))
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (Boolean.TRUE.equals(requestData.getIsDefault()) && !Boolean.TRUE.equals(entity.getIsDefault())) {
            unsetCurrentDefault(user);
        }

        addressEntityMapper.updateEntity(requestData, entity);
        return addressDataMapper.toResponseData(addressRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteAddress(String firebaseUid, Integer id) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);

        ShippingAddressEntity entity = addressRepository.findById(id)
                .filter(a -> a.getUser().getUid().equals(user.getUid()))
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        addressRepository.delete(entity);
    }

    @Override
    @Transactional
    public ShippingAddressResponseData setDefaultAddress(String firebaseUid, Integer id) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);

        ShippingAddressEntity entity = addressRepository.findById(id)
                .filter(a -> a.getUser().getUid().equals(user.getUid()))
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (!Boolean.TRUE.equals(entity.getIsDefault())) {
            unsetCurrentDefault(user);
            entity.setIsDefault(true);
            addressRepository.save(entity);
        }

        return addressDataMapper.toResponseData(entity);
    }

    private void unsetCurrentDefault(UserEntity user) {
        ShippingAddressEntity currentDefault = addressRepository.findByUserAndIsDefaultTrue(user);
        if (currentDefault != null) {
            currentDefault.setIsDefault(false);
            addressRepository.save(currentDefault);
        }
    }
}
