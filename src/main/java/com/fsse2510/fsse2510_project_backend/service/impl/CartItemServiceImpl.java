package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.request.CartItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.cartitem.entity.CartItemEntity;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductInventoryEntity;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.CartItemMappingException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.CartItemNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.InvalidQuantityException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.NotEnoughStockException;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.cartItem.CartItemEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.CartItemRepository;
import com.fsse2510.fsse2510_project_backend.repository.ProductInventoryRepository;
import com.fsse2510.fsse2510_project_backend.repository.UserRepository;
import com.fsse2510.fsse2510_project_backend.service.CartItemService;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductInventoryResponseData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.mapper.product.ProductInventoryMapper;
import com.fsse2510.fsse2510_project_backend.service.CartPromotionEnricherService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private static final Logger logger = LoggerFactory.getLogger(CartItemServiceImpl.class);
    //Prevent evil input
    private static final int MAX_QUANTITY_PER_ITEM = 200;

    private final CartItemRepository cartItemRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final UserRepository userRepository;

    private final UserService userService;
    private final CartItemEntityMapper cartItemEntityMapper;
    private final ProductInventoryMapper productInventoryMapper;
    private final CartPromotionEnricherService cartPromotionEnricherService;

    @Override
    @Transactional
    public List<CartItemResponseData> addCartItem(CartItemRequestData requestData) {
        processAddCartItem(requestData);
        return getUserCart(requestData.getUser());
    }

    //API not used by frontend
    @Override
    @Transactional
    public List<CartItemResponseData> addCartItems(List<CartItemRequestData> requestDataList) {
        if (requestDataList == null || requestDataList.isEmpty()) {
            return List.of();
        }

        for (CartItemRequestData requestData : requestDataList) {
            processAddCartItem(requestData);
        }

        return getUserCart(requestDataList.getFirst().getUser());
    }

    private void processAddCartItem(CartItemRequestData requestData) {
        UserData userData = userService.getOrCreateUser(requestData.getUser());
        ProductInventoryEntity inventoryEntity = getProductInventoryEntity(requestData.getSku());
        ProductInventoryResponseData inventoryData = productInventoryMapper.toResponseData(inventoryEntity);
        // Block invalid quantity requests before query
        validateQuantityAndStock(inventoryData, requestData.getQuantity(), userData);
        // CartItem need UserEntity(@ManyToOne), use proxy to ref UserEntity
        // Save round trip to fetch again
        UserEntity userRef = userRepository.getReferenceById(userData.getUid());

        Optional<CartItemEntity> existingItem = cartItemRepository.findByUserAndProductInventory(userRef,
                inventoryEntity);

        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            //User cart already have this item, check updated qty
            //Assume inputs are evil & MAX_QUANTITY_PER_ITEM is not properly set in future
            //Typecast long and block int overflow
            long potentialQuantity = (long) item.getQuantity() + requestData.getQuantity();

            if (potentialQuantity > MAX_QUANTITY_PER_ITEM) {
                logger.warn("Add Item Failed: Total quantity exceeds limit. User={}, Sku={}, Current={}, Add={}",
                        userData.getUid(), inventoryData.getSku(), item.getQuantity(), requestData.getQuantity());
                throw new InvalidQuantityException("Total quantity cannot exceed " + MAX_QUANTITY_PER_ITEM);
            }
            if (inventoryData.getStock() < potentialQuantity) {
                logger.warn("Add Cart Item Failed: Total quantity exceeds stock. Sku={}, Stock={}, TotalRequested={}",
                        inventoryData.getSku(), inventoryData.getStock(), potentialQuantity);
                throw new NotEnoughStockException("Not enough stock for total quantity");
            }
            //Typecast back to int, update qty
            item.setQuantity((int) potentialQuantity);
        } else {
            CartItemEntity newItem = cartItemEntityMapper.toEntity(requestData.getQuantity(), userRef,
                    inventoryEntity);
            if (newItem != null) {
                cartItemRepository.save(newItem);
            } else {
                logger.error("Failed to map CartItemEntity for SKU: {}", inventoryData.getSku());
                throw new CartItemMappingException("Cart item creation failed");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponseData> getUserCart(FirebaseUserData firebaseUser) {
        UserData userData = userService.getOrCreateUser(firebaseUser); // Get ID via Data
        UserEntity userRef = userRepository.getReferenceById(userData.getUid()); // Proxy

        List<CartItemEntity> entities = cartItemRepository.findAllByUserWithProduct(userRef);

        // Use stream removes boilerplate code
        // Immutability
        List<CartItemResponseData> cartItems = entities.stream()
                .map(cartItemEntityMapper::toResponseData)
                .toList();

        // Enrich with promotional pricing
        return cartPromotionEnricherService.enrichWithPromotions(cartItems, userRef);
    }

    @Override
    @Transactional
    public List<CartItemResponseData> updateCartItemQuantity(CartItemRequestData requestData) {
        UserData userData = userService.getOrCreateUser(requestData.getUser());
        ProductInventoryEntity inventoryEntity = getProductInventoryEntity(requestData.getSku());
        ProductInventoryResponseData inventoryData = productInventoryMapper.toResponseData(inventoryEntity);

        UserEntity userRef = userRepository.getReferenceById(userData.getUid());

        CartItemEntity item = cartItemRepository.findByUserAndProductInventory(userRef, inventoryEntity)
                .orElseThrow(() -> {
                    logger.warn("Update Cart Item Failed: Item not found. User={}, Sku={}", userData.getUid(),
                            inventoryData.getSku());
                    return new CartItemNotFoundException("Cart item not found");
                });

        validateQuantityAndStock(inventoryData, requestData.getQuantity(), userData);

        item.setQuantity(requestData.getQuantity());
        cartItemRepository.save(item);

        return getUserCart(requestData.getUser());
    }

    @Override
    @Transactional
    public List<CartItemResponseData> removeCartItem(CartItemRequestData requestData) {
        UserData userData = userService.getOrCreateUser(requestData.getUser());
        ProductInventoryEntity inventory = getProductInventoryEntity(requestData.getSku());

        UserEntity userRef = userRepository.getReferenceById(userData.getUid());

        cartItemRepository.deleteByUserAndProductInventory(userRef, inventory);
        logger.info("Cart Item Removed: User={}, Sku={}", userData.getUid(), inventory.getSku());
        return getUserCart(requestData.getUser());
    }

    @Override
    @Transactional
    public void clearCart(UserEntity userRef) {
        cartItemRepository.deleteAllByUser(userRef);
        logger.info("Cart cleared for User={}", userRef.getUid());
    }

    private ProductInventoryEntity getProductInventoryEntity(String sku) {
        return productInventoryRepository.findBySku(sku)
                .orElseThrow(() -> {
                    logger.warn("Product Inventory not found for SKU: {}", sku);
                    return new ProductNotFoundException("Product not found for SKU: " + sku);
                });
    }

    /*
     * Common Validation Logic:
     * 1. Check quantity > 0
     * 2. Check quantity <= MAX_LIMIT
     * 3. Check stock >= quantity
     */
    private void validateQuantityAndStock(
            ProductInventoryResponseData inventory,
            Integer quantity,
            UserData user) {

        if (quantity <= 0) {
            logger.warn("Validation Failed: Quantity must be > 0. User={}, Sku={}", user.getUid(), inventory.getSku());
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }

        if (quantity > MAX_QUANTITY_PER_ITEM) {
            logger.warn("Validation Failed: Quantity Exceeds Limit. User={}, Sku={}, Qty={}",
                    user.getUid(), inventory.getSku(), quantity);
            throw new InvalidQuantityException("Quantity cannot exceed " + MAX_QUANTITY_PER_ITEM);
        }

        if (inventory.getStock() < quantity) {
            logger.warn("Validation Failed: Not enough stock. Sku={}, Stock={}, Requested={}",
                    inventory.getSku(), inventory.getStock(), quantity);
            throw new NotEnoughStockException("Not enough stock");
        }
    }
}
