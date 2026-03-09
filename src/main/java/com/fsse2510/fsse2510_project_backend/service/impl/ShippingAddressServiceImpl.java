package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.address.dto.request.CreateShippingAddressRequestDto;
import com.fsse2510.fsse2510_project_backend.data.address.dto.response.ShippingAddressResponseDto;
import com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.exception.address.AddressNotFoundException;
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

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressResponseDto> getAllAddresses(String firebaseUid) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);
        return addressRepository.findByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingAddressResponseDto createAddress(String firebaseUid, CreateShippingAddressRequestDto requestDto) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);

        if (Boolean.TRUE.equals(requestDto.getIsDefault())) {
            unsetCurrentDefault(user);
        }

        ShippingAddressEntity entity = ShippingAddressEntity.builder()
                .user(user)
                .recipientName(requestDto.getRecipientName())
                .phoneNumber(requestDto.getPhoneNumber())
                .addressLine1(requestDto.getAddressLine1())
                .addressLine2(requestDto.getAddressLine2())
                .city(requestDto.getCity())
                .stateProvince(requestDto.getStateProvince())
                .postalCode(requestDto.getPostalCode())
                .isDefault(requestDto.getIsDefault())
                .build();

        return mapToDto(addressRepository.save(entity));
    }

    @Override
    @Transactional
    public ShippingAddressResponseDto updateAddress(String firebaseUid, Integer id,
            CreateShippingAddressRequestDto requestDto) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);

        ShippingAddressEntity entity = addressRepository.findById(id)
                .filter(a -> a.getUser().getUid().equals(user.getUid()))
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (Boolean.TRUE.equals(requestDto.getIsDefault()) && !Boolean.TRUE.equals(entity.getIsDefault())) {
            unsetCurrentDefault(user);
        }

        entity.setRecipientName(requestDto.getRecipientName());
        entity.setPhoneNumber(requestDto.getPhoneNumber());
        entity.setAddressLine1(requestDto.getAddressLine1());
        entity.setAddressLine2(requestDto.getAddressLine2());
        entity.setCity(requestDto.getCity());
        entity.setStateProvince(requestDto.getStateProvince());
        entity.setPostalCode(requestDto.getPostalCode());
        entity.setIsDefault(requestDto.getIsDefault());

        return mapToDto(addressRepository.save(entity));
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
    public ShippingAddressResponseDto setDefaultAddress(String firebaseUid, Integer id) {
        UserEntity user = userService.findEntityByFirebaseUid(firebaseUid);

        ShippingAddressEntity entity = addressRepository.findById(id)
                .filter(a -> a.getUser().getUid().equals(user.getUid()))
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (!Boolean.TRUE.equals(entity.getIsDefault())) {
            unsetCurrentDefault(user);
            entity.setIsDefault(true);
            addressRepository.save(entity);
        }

        return mapToDto(entity);
    }

    private void unsetCurrentDefault(UserEntity user) {
        ShippingAddressEntity currentDefault = addressRepository.findByUserAndIsDefaultTrue(user);
        if (currentDefault != null) {
            currentDefault.setIsDefault(false);
            addressRepository.save(currentDefault);
        }
    }

    private ShippingAddressResponseDto mapToDto(ShippingAddressEntity entity) {
        return ShippingAddressResponseDto.builder()
                .id(entity.getId())
                .recipientName(entity.getRecipientName())
                .phoneNumber(entity.getPhoneNumber())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .stateProvince(entity.getStateProvince())
                .postalCode(entity.getPostalCode())
                .isDefault(entity.getIsDefault())
                .build();
    }
}
