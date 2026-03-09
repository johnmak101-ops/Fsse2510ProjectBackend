package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.address.dto.request.CreateShippingAddressRequestDto;
import com.fsse2510.fsse2510_project_backend.data.address.dto.response.ShippingAddressResponseDto;
import com.fsse2510.fsse2510_project_backend.service.ShippingAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class ShippingAddressController {
    private final ShippingAddressService addressService;

    private String getFirebaseUid(JwtAuthenticationToken token) {
        return token.getToken().getSubject();
    }

    @GetMapping
    public List<ShippingAddressResponseDto> getAllAddresses(JwtAuthenticationToken token) {
        return addressService.getAllAddresses(getFirebaseUid(token));
    }

    @PostMapping
    public ShippingAddressResponseDto createAddress(JwtAuthenticationToken token,
            @Valid @RequestBody CreateShippingAddressRequestDto requestDto) {
        return addressService.createAddress(getFirebaseUid(token), requestDto);
    }

    @PutMapping("/{id}")
    public ShippingAddressResponseDto updateAddress(JwtAuthenticationToken token,
            @PathVariable Integer id,
            @Valid @RequestBody CreateShippingAddressRequestDto requestDto) {
        return addressService.updateAddress(getFirebaseUid(token), id, requestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(JwtAuthenticationToken token, @PathVariable Integer id) {
        addressService.deleteAddress(getFirebaseUid(token), id);
    }

    @PatchMapping("/{id}/default")
    public ShippingAddressResponseDto setDefaultAddress(JwtAuthenticationToken token, @PathVariable Integer id) {
        return addressService.setDefaultAddress(getFirebaseUid(token), id);
    }
}
