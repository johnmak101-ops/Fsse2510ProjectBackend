package com.fsse2510.fsse2510_project_backend.data.promotion.promotionType;

public enum PromotionType {
    // --- Item Level (Specific to products) ---
    SPECIFIC_PRODUCT_DISCOUNT, // Specific product discount
    SPECIFIC_CATEGORY_DISCOUNT, // Specific category discount
    SPECIFIC_COLLECTION_DISCOUNT, // Specific collection discount
    SPECIFIC_TAG_DISCOUNT, // Specific tag discount
    BUY_X_GET_Y_FREE, // Buy X get Y free (e.g., Buy 2 Get 1 Free)
    BUNDLE_DISCOUNT, // Bundle discount (e.g., Buy 3 Get 20% Off)

    // --- Order Level (Specific to entire order) ---
    MIN_QUANTITY_DISCOUNT, // Quantity-based promotion (e.g., 10% off for 5+ items)
    MIN_AMOUNT_DISCOUNT, // Amount-based promotion (e.g., $50 off for $500+)

    // --- Contextual (Identity/Environment) ---
    MEMBERSHIP_DISCOUNT, // Membership promotion
    STOREWIDE_SALE // Storewide sale (e.g., 20% off all items)
}
