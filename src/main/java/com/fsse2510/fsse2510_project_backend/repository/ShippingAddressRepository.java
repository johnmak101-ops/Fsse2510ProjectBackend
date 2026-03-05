package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddressEntity, Integer> {
    List<ShippingAddressEntity> findByUser(UserEntity user);

    // Find previously set default address to unset it
    ShippingAddressEntity findByUserAndIsDefaultTrue(UserEntity user);
}
