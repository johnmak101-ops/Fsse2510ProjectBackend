package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.request.CartItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.cartitem.dto.request.CartItemBatchRequestDto;
import com.fsse2510.fsse2510_project_backend.data.cartitem.dto.response.CartItemResponseDto;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.mapper.cartItem.CartItemDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.cartItem.CartItemDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.CartItemService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Validated
public class CartItemController {

        private final CartItemService cartItemService;
        private final CartItemDataMapper cartItemDataMapper;
        private final CartItemDtoMapper cartItemDtoMapper;

        /**
         * Adds a specific quantity of a product to the user's cart.
         * <p>
         * Endpoint: PUT /cart/items/{sku}/{quantity}
         * </p>
         *
         * @param sku      The SKU of the product to add.
         * @param quantity The quantity to add (must be greater than 0).
         * @param token    The authenticated user's JWT token.
         * @return A list of CartItemResponseDto representing the updated cart.
         */
        @PutMapping("/items/{sku}/{quantity}")
        public List<CartItemResponseDto> addCartItem(@PathVariable String sku,
                        @PathVariable @Positive(message = "Quantity must be greater than 0") Integer quantity,
                        JwtAuthenticationToken token) {
                return cartItemService.addCartItem(
                                cartItemDataMapper.toRequestData(sku, quantity, getFirebaseUser(token)))
                                .stream()
                                .map(cartItemDtoMapper::toResponseDto)
                                .toList();
        }

        /**
         * API NOT USED BY FRONTEND, CART PAGE HAVE AUTH GUARD
         * Adds multiple items in LOCAL STORAGE to the user's cart in a batch.
         * <p>
         * Endpoint: PUT /cart/items/batch
         * </p>
         *
         * @param batchItems List of items to add, each containing SKU and quantity.
         * @param token      The authenticated user's JWT token.
         * @return A list of CartItemResponseDto representing the updated cart.
         */
        @PutMapping("/items/batch")
        public List<CartItemResponseDto> addCartItemsBatch(
                        @RequestBody List<CartItemBatchRequestDto> batchItems,
                        JwtAuthenticationToken token) {
                List<CartItemRequestData> requestDataList = batchItems
                                .stream()
                                .map(item -> cartItemDataMapper.toRequestData(
                                                item.getSku(),
                                                item.getQuantity(),
                                                getFirebaseUser(token)))
                                .toList();

                return cartItemService.addCartItems(requestDataList)
                                .stream()
                                .map(cartItemDtoMapper::toResponseDto)
                                .toList();
        }

        /**
         * Retrieves the current user's shopping cart.
         * <p>
         * Endpoint: GET /cart/items
         * </p>
         *
         * @param token The authenticated user's JWT token.
         * @return A list of CartItemResponseDto representing the items in the cart.
         */
        @GetMapping("/items")
        public List<CartItemResponseDto> getUserCart(JwtAuthenticationToken token) {
                return cartItemService.getUserCart(getFirebaseUser(token)).stream()
                                .map(cartItemDtoMapper::toResponseDto)
                                .toList();
        }

        /**
         * Updates the quantity of a specific item in the cart.
         * <p>
         * Endpoint: PATCH /cart/items/{sku}/{quantity}
         * </p>
         *
         * @param sku      The SKU of the product to update.
         * @param quantity The new quantity (must be greater than 0).
         * @param token    The authenticated user's JWT token.
         * @return A list of CartItemResponseDto representing the updated cart.
         */
        @PatchMapping("/items/{sku}/{quantity}")
        public List<CartItemResponseDto> updateCartItemQuantity(@PathVariable String sku,
                        @PathVariable @Positive(message = "Quantity must be greater than 0") Integer quantity,
                        JwtAuthenticationToken token) {
                return cartItemService.updateCartItemQuantity(
                                cartItemDataMapper.toRequestData(
                                                sku,
                                                quantity,
                                                getFirebaseUser(token)))
                                .stream()
                                .map(cartItemDtoMapper::toResponseDto)
                                .toList();
        }

        /**
         * Removes a specific item from the cart.
         * <p>
         * Endpoint: DELETE /cart/items/{sku}
         * </p>
         *
         * @param sku   The SKU of the product to remove.
         * @param token The authenticated user's JWT token.
         * @return A list of CartItemResponseDto representing the updated cart.
         */
        @DeleteMapping("/items/{sku}")
        public List<CartItemResponseDto> removeCartItem(@PathVariable String sku,
                        JwtAuthenticationToken token) {
                return cartItemService.removeCartItem(
                                cartItemDataMapper.toRequestData(
                                                sku,
                                                0,
                                                getFirebaseUser(token)))
                                .stream()
                                .map(cartItemDtoMapper::toResponseDto)
                                .toList();
        }

        private FirebaseUserData getFirebaseUser(JwtAuthenticationToken token) {
                return FirebaseUserData.builder()
                        .firebaseUid(token.getToken().getSubject())
                        .email((String) token.getTokenAttributes().get("email"))
                        .build();
        }
}
