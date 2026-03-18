# Test Cases Document

## FSSE2510 E-Commerce Platform

| Item               | Detail                  |
|--------------------|-------------------------|
| **Document Version** | 1.0                   |
| **Project Name**     | FSSE2510 E-Commerce   |

---

## 1. Introduction
This document outlines the high-level test cases (positive, negative, and edge cases) for the core functional modules of the FSSE2510 e-commerce platform.

---

## 2. Authentication & Authorization

### TC-AUTH-01: Valid Firebase Login
- **Precondition**: User has a valid Google/Email account via Firebase.
- **Steps**: User logs in frontend -> Send Firebase JWT to `GET /api/user`.
- **Expected Result**: 200 OK. Backend creates User record if it doesn't exist, or returns the existing profile.

### TC-AUTH-02: Invalid/Expired Firebase JWT (Edge Case)
- **Precondition**: User attempts to use an expired or malformed JWT token.
- **Steps**: User calls `GET /api/user` with invalid token.
- **Expected Result**: 401 Unauthorized. Access denied.

### TC-AUTH-02: Missing or Invalid Token
- **Precondition**: None.
- **Steps**: Call `GET /api/user` with no token, an expired token, or a malformed token.
- **Expected Result**: 401 Unauthorized. Access denied.

### TC-AUTH-03: Role-Based Access Control (Admin)
- **Precondition**: User is a standard customer (no `ROLE_ADMIN`).
- **Steps**: Call `GET /api/admin/user`.
- **Expected Result**: 403 Forbidden. User cannot access admin endpoints.

---

## 3. Product & Inventory Management

### TC-PROD-01: Create Product (Happy Path)
- **Precondition**: User is Admin.
- **Steps**: Call `POST /api/admin/product` with valid JSON payload including variants.
- **Expected Result**: 200 OK. Product and variants are saved.

### TC-PROD-02: Duplicate SKU Validation
- **Precondition**: User is Admin. Variant SKU `TEE-BLK-M` exists.
- **Steps**: Try to create a new variant with SKU `TEE-BLK-M`.
- **Expected Result**: 409 Conflict. Cannot have duplicate SKUs.

### TC-PROD-03: View Public Product Catalog
- **Precondition**: None (Guest user).
- **Steps**: Call `GET /api/public/product`.
- **Expected Result**: 200 OK. Returns paginated list of active products. Hidden products are excluded.

---

## 4. Cart & Checkout

### TC-CART-01: Add Item to Cart (Happy Path)
- **Precondition**: User has empty cart. `A1B2` has 10 stock.
- **Steps**: Call `POST /api/cart/A1B2/2`.
- **Expected Result**: 200 OK. Cart subtotal updates based on the variant's current price.

### TC-CART-02: Add Item Exceeding Stock
- **Precondition**: `A1B2` has 2 stock available.
- **Steps**: Call `POST /api/cart/A1B2/3`.
- **Expected Result**: 400 Bad Request. Item cannot be added due to insufficient stock.

### TC-CART-03: Prepare Checkout (Reserve Stock)
- **Precondition**: Cart has 1 item (`A1B2`, qty 1). Shipping address is provided.
- **Steps**: Call `POST /api/transaction/prepare`.
- **Expected Result**: 200 OK. The `stock` of `A1B2` decreases by 1, and `reservedStock` increases by 1. Transaction is created in `PENDING` status.

---

## 5. Stripe Webhook & Payment Reconciliation

### TC-PAY-01: Successful Payment Webhook
- **Precondition**: Transaction #123 is `PENDING`.
- **Steps**: Stripe sends `checkout.session.completed` webhook with valid signature and payment intent matching Transaction #123.
- **Expected Result**: 200 OK. Transaction #123 is marked `SUCCESS`. The 1 `reservedStock` for `A1B2` is permanently deducted. Memberships points are awarded.

### TC-PAY-02: Forged Webhook Signature
- **Precondition**: Transaction #123 is `PENDING`.
- **Steps**: Send POST to `/api/webhook/stripe` with a fake signature header.
- **Expected Result**: 400 Bad Request. System rejects the payload. Transaction remains `PENDING`.

### TC-PAY-03: Stale Transaction Cleanup Job
- **Precondition**: Transaction #124 has been `PENDING` for 35 minutes.
- **Steps**: The `abortStalePendingTransactions` scheduler runs.
- **Expected Result**: Transaction #124 becomes `ABORTED`. The reserved stock for Transaction #124's items is moved back to available stock.

---

## 6. Promotions & Coupons

### TC-PROMO-01: Apply Valid Coupon
- **Precondition**: Coupon `SUMMER20` is active. Min spend $100. Cart subtotal is $150.
- **Steps**: Call `POST /api/transaction/prepare` with `SUMMER20`.
- **Expected Result**: 200 OK. Transaction total mathematically reflects the 20% discount.

### TC-PROMO-02: Apply Unqualified Coupon (Min Spend)
- **Precondition**: Coupon `SUMMER20` min spend $100. Cart subtotal is $50.
- **Steps**: Call `POST /api/transaction/prepare` with `SUMMER20`.
- **Expected Result**: 400 Bad Request. "Minimum spend not met".

### TC-PROMO-03: Auto-apply Product Promotion
- **Precondition**: Admin sets a "20% off all T-Shirts" promotion.
- **Steps**: Customer adds a T-Shirt to the cart and views cart items.
- **Expected Result**: 200 OK. The backend API automatically overrides the cart item price to reflect the 20% discount without the user typing a code.

### TC-PROMO-04: Promotion Stacking Conflict
- **Precondition**: Item A has a "10% off" promotion. User enters a "20% off" coupon. promotions `canStackWithCoupon` is false.
- **Steps**: Call `POST /api/transaction/prepare` with the coupon.
- **Expected Result**: 200 OK. The system applies the 20% coupon to Item A and ignores the weaker 10% promotion, delivering the best price to the user without stacking.

### TC-PROMO-05: Expired Coupon or Quota Reached (Edge Case)
- **Precondition**: Coupon `WINTER10` is expired, or its global usage quota is completely consumed.
- **Steps**: Call `POST /api/transaction/prepare` with `WINTER10`.
- **Expected Result**: 400 Bad Request. "Coupon invalid, expired, or usage limit reached."

---

## 7. Membership & Loyalty

### TC-MEMB-01: Auto-Upgrade Tier (Spending Threshold Met)
- **Precondition**: Gold tier requires total spending of $5000. User's current cycle spending is $4800 (Silver tier).
- **Steps**: User Completes a $300 payment (Transaction -> SUCCESS).
- **Expected Result**: User's total cycle spending becomes $5100. User is immediately upgraded to Gold tier.

### TC-MEMB-02: Point Accrual Calculation
- **Precondition**: User is Silver tier (Point Multiplier = 1.5x). User spends $100.
- **Steps**: Payment succeeds.
- **Expected Result**: User's reward points balance increases by 150 points. Transaction history records points earned.

### TC-MEMB-03: Tier Evaluation (No Upgrade)
- **Precondition**: Silver tier requires $1000. User has $500 spending.
- **Steps**: User completes a $200 payment.
- **Expected Result**: User's spending becomes $700. Tier remains BRONZE.

### TC-MEMB-04: Spending Cycle Expiry (Enter Grace Period)
- **Precondition**: User is Gold Tier, 12-month `SpendingCycle` ends today. They spent $500 in the new cycle timeframe (Bronze qualifying).
- **Steps**: System scheduler evaluates expired cycles.
- **Expected Result**: User's tier remains Gold, but `gracePeriodEndDate` is set to 30 days from now. A new `SpendingCycle` begins tracking spending.

### TC-MEMB-05: Grace Period Expiry & Downgrade
- **Precondition**: User is in Grace Period (Old tier: Gold). `gracePeriodEndDate` is today. New cycle spending is $500.
- **Steps**: System scheduler evaluates expired grace periods.
- **Expected Result**: User is downgraded to Bronze tier based on the real spending of the new cycle.

---

## 8. User Profile & Preferences

### TC-USER-01: Manage Shipping Address (Default)
- **Precondition**: User is logged in and has 0 addresses.
- **Steps**: User creates a new address via `POST /api/user/shippingaddress` and sets `isDefault = true`.
- **Expected Result**: 200 OK. Address is saved as the default.

### TC-USER-02: Toggle Wishlist Item
- **Precondition**: User is logged in. `A1B2` is not in wishlist.
- **Steps**: User calls `POST /api/wishlist/A1B2`.
- **Expected Result**: 200 OK. `A1B2` is added to wishlist. Calling the endpoint again removes it.

### TC-USER-03: Wishlist Invalid Product (Edge Case)
- **Precondition**: Product `XYZ99` does not exist in the database.
- **Steps**: User calls `POST /api/wishlist/XYZ99`.
- **Expected Result**: 400 Bad Request. "Product not found".

---

## 9. Content Management System (CMS) & Admin

### TC-CMS-01: Build Navigation Tree
- **Precondition**: Admin creates Parent Node "Mens", Child Node "Tops" (parentId = Mens.id).
- **Steps**: Call `GET /api/public/navigation/tree`.
- **Expected Result**: 200 OK. Backend structurally nests the "Tops" object inside the "Mens" `children` array.

### TC-CMS-02: Homepage Showcase Collection
- **Precondition**: Admin sets Banner A to display the 'Summer26' tag.
- **Steps**: Guest queries `GET /api/public/showcase`.
- **Expected Result**: 200 OK. Banner A is returned, along with a subset of active products tagged with 'Summer26'.
