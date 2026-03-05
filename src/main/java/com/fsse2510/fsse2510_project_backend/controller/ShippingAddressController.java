package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.address.dto.request.CreateShippingAddressRequestDto;
import com.fsse2510.fsse2510_project_backend.data.address.dto.response.ShippingAddressResponseDto;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.service.ShippingAddressService;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class ShippingAddressController {
    private final ShippingAddressService addressService;
    private final UserService userService;

    private UserEntity getCurrentUser(JwtAuthenticationToken token) {
        return userService.findEntityByFirebaseUid(token.getToken().getSubject());
    }

    @GetMapping
    public List<ShippingAddressResponseDto> getAllAddresses(JwtAuthenticationToken token) {
        return addressService.getAllAddresses(getCurrentUser(token));
    }

    @PostMapping
    public ShippingAddressResponseDto createAddress(JwtAuthenticationToken token,
            @Valid @RequestBody CreateShippingAddressRequestDto requestDto) {
        return addressService.createAddress(getCurrentUser(token), requestDto);
    }

    @PutMapping("/{id}")
    public ShippingAddressResponseDto updateAddress(JwtAuthenticationToken token,
            @PathVariable Integer id,
            @Valid @RequestBody CreateShippingAddressRequestDto requestDto) {
        return addressService.updateAddress(getCurrentUser(token), id, requestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(JwtAuthenticationToken token, @PathVariable Integer id) {
        addressService.deleteAddress(getCurrentUser(token), id);
    }

    @PatchMapping("/{id}/default")
    public ShippingAddressResponseDto setDefaultAddress(JwtAuthenticationToken token, @PathVariable Integer id) {
        return addressService.setDefaultAddress(getCurrentUser(token), id);
    }
}
