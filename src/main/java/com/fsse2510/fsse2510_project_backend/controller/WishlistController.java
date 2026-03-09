package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.wishlist.dto.response.WishlistResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.wishlist.WishlistDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
/**
 * Controller for managing the user's wishlist.
 * <p>
 * Handles adding, retrieving, and removing products from the wishlist.
 * </p>
 */
public class WishlistController {
    private final WishlistService wishlistService;
    private final WishlistDtoMapper wishlistDtoMapper;

    private FirebaseUserData getFirebaseUser(JwtAuthenticationToken token) {
        return FirebaseUserData.builder()
                .firebaseUid(token.getToken().getSubject())
                .email((String) token.getTokenAttributes().get("email"))
                .build();
    }

    /**
     * Adds a product to the user's wishlist.
     * <p>
     * Endpoint: POST /wishlist/{pid}
     * </p>
     *
     * @param pid   The product ID to add.
     * @param token The JWT authentication token.
     */
    @PostMapping("/{pid}")
    public void addWishlistItem(@PathVariable Integer pid, JwtAuthenticationToken token) {
        wishlistService.addWishlistItem(getFirebaseUser(token), pid);
    }

    /**
     * Retrieves the current user's wishlist.
     * <p>
     * Endpoint: GET /wishlist
     * </p>
     *
     * @param token The JWT authentication token.
     * @return A list of WishlistResponseDto representing the wishlist items.
     */
    @GetMapping
    public List<WishlistResponseDto> getUserWishlist(JwtAuthenticationToken token) {
        return wishlistService.getUserWishlist(getFirebaseUser(token)).stream()
                .map(wishlistDtoMapper::toResponseDto)
                .toList();
    }

    /**
     * Removes a product from the user's wishlist.
     * <p>
     * Endpoint: DELETE /wishlist/{pid}
     * </p>
     *
     * @param pid   The product ID to remove.
     * @param token The JWT authentication token.
     */
    @DeleteMapping("/{pid}")
    public void removeWishlistItem(@PathVariable Integer pid, JwtAuthenticationToken token) {
        wishlistService.removeWishlistItem(getFirebaseUser(token), pid);
    }
}
