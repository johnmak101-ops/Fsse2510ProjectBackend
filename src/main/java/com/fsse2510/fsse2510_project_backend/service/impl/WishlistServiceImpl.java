package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.data.wishlist.domainObject.response.WishlistResponseData;
import com.fsse2510.fsse2510_project_backend.data.wishlist.entity.WishlistEntity;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.wishlist.WishlistEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.UserRepository;
import com.fsse2510.fsse2510_project_backend.repository.WishlistRepository;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import com.fsse2510.fsse2510_project_backend.service.WishlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private static final Logger logger = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WishlistEntityMapper wishlistEntityMapper;

    @Override
    @Transactional
    public void addWishlistItem(FirebaseUserData firebaseUser, Integer pid) {
        UserEntity user = getUserEntity(firebaseUser);
        ProductEntity product = productRepository.findById(pid)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + pid));

        if (wishlistRepository.findByUserAndProduct(user, product).isPresent()) {
            logger.info("Product already in wishlist. User={}, Pid={}", user.getUid(), pid);
            return;
        }

        WishlistEntity entity = wishlistEntityMapper.toEntity(user, product);
        wishlistRepository.save(entity);
    }

    @Override
    public List<WishlistResponseData> getUserWishlist(FirebaseUserData firebaseUser) {
        UserEntity user = getUserEntity(firebaseUser);
        return wishlistRepository.findAllByUser(user).stream()
                .map(wishlistEntityMapper::toResponseData)
                .toList();
    }

    @Override
    @Transactional
    public void removeWishlistItem(FirebaseUserData firebaseUser, Integer pid) {
        UserEntity user = getUserEntity(firebaseUser);
        ProductEntity product = productRepository.findById(pid)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + pid));

        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    private UserEntity getUserEntity(FirebaseUserData firebaseUser) {
        UserData userDomain = userService.getOrCreateUser(firebaseUser);
        return userRepository.getReferenceById(userDomain.getUid());
    }
}
