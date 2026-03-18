# Sequence Diagrams — Backend Key Flows

> **Version:** 2.0 | **Date:** 2026-03-18

---

## 1. User Authentication Flow

```mermaid
sequenceDiagram
    actor User
    participant FE as Next.js Frontend
    participant FB as Firebase Auth
    participant BE as Spring Boot Backend

    User->>FE: Click "Sign In with Google"
    FE->>FB: signInWithPopup(GoogleAuthProvider)
    FB->>User: Google OAuth Consent Screen
    User->>FB: Authorize
    FB-->>FE: Firebase ID Token (JWT)
    FE->>BE: GET /users/me (Authorization: Bearer {JWT})
    BE->>BE: Spring Security validates JWT
    BE->>BE: Extract email → Check ADMIN_EMAILS
    BE->>BE: Find or Create UserEntity by firebaseUid

    alt New User
        BE->>BE: Create UserEntity (level=NO_MEMBERSHIP)
    end

    BE-->>FE: UserResponseDto (profile + membership info)
```

---

## 2. Add to Cart Flow

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend
    participant BE as Backend API

    User->>FE: Click "Add to Cart" (pid, sku, qty)
    FE->>BE: POST /cart {pid, sku, quantity}
    
    alt API Success
        BE->>BE: Check stock availability
        BE->>BE: Merge or create cart item (by SKU)
        BE-->>FE: CartItemDto
    end

    alt API Failure (e.g., out of stock)
        BE-->>FE: 409 Conflict
    end
```

---

## 3. Complete Checkout Flow

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend
    participant BE as Backend
    participant Stripe as Stripe API
    participant WH as Stripe Webhook

    User->>FE: Click "Place Order"
    FE->>BE: POST /transactions {couponCode, usePoints, shippingAddressId}
    
    Note over BE: Validate cart, stock, coupon, points
    Note over BE: Create TransactionEntity (PREPARE)
    Note over BE: Snapshot shipping address
    Note over BE: Snapshot product details → transaction_product
    
    BE-->>FE: TransactionResponseDto (tid, total)
    
    User->>FE: Click "Pay Now"
    FE->>BE: POST /transactions/{tid}/payment
    
    Note over BE: Update status → PENDING
    BE->>Stripe: Create PaymentIntent (amount, currency)
    Stripe-->>BE: PaymentIntent (client_secret)
    BE-->>FE: PaymentResponseDto (clientSecret)
    
    FE->>Stripe: confirmPayment(clientSecret)
    Stripe-->>FE: Payment result
    
    alt Payment Succeeded
        FE->>BE: PATCH /transactions/{tid}/processing
        FE->>BE: PATCH /transactions/{tid}/success
    end

    alt Payment Failed
        FE->>BE: PATCH /transactions/{tid}/fail
    end

    Note over WH: Async Webhook (backup confirmation)
    Stripe->>WH: POST /webhooks/stripe
    
    alt payment_intent.succeeded
        WH->>BE: Verify signature → Mark SUCCESS
        Note over BE: Deduct stock, Award points
        Note over BE: Update membership spending
    end

    alt payment_intent.payment_failed
        WH->>BE: Mark FAILED, Release reserved stock
    end
```

---

## 4. Coupon Validation Flow

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant BE as Backend

    FE->>BE: GET /public/coupon/validate?code=SUMMER2025
    
    Note over BE: Check is_active = true
    Note over BE: Check valid_until >= today
    Note over BE: Check usage_count < usage_limit
    Note over BE: Check user membership >= required_tier
    
    alt Valid Coupon
        BE-->>FE: {discountType, discountValue, description}
    end

    alt Invalid Coupon
        BE-->>FE: 400 {message: "Coupon expired"}
    end
```

---

## 5. Membership Tier Upgrade Flow

```mermaid
sequenceDiagram
    participant TX as Transaction Service
    participant MS as Membership Service
    participant DB as Database

    TX->>TX: Transaction marked SUCCESS
    TX->>MS: updateMemberSpending(user, orderTotal)
    
    MS->>DB: Load user's current level + accumulated_spending
    MS->>MS: accumulated_spending += orderTotal
    MS->>MS: cycle_spending += orderTotal
    
    MS->>DB: Load MembershipConfig for next tier
    
    alt Spending meets next tier threshold
        MS->>MS: user.level = nextTier
        MS->>MS: Reset cycle_end_date to +1 year
        MS->>DB: Save updated user
        Note over MS: Points rate increases for future orders
    end

    MS->>MS: Calculate earned points = orderTotal × pointRate
    MS->>MS: user.points += earnedPoints
    MS->>DB: Save user points
```

---

## 6. Stock Reserve & Deduction Flow

```mermaid
sequenceDiagram
    participant TX as Transaction Service
    participant INV as Inventory Service
    participant DB as Database

    Note over TX: Create Transaction
    TX->>INV: Check stock availability
    INV->>DB: SELECT stock, stock_reserved FROM product_inventory WHERE sku = ?
    DB-->>INV: {stock: 10, stock_reserved: 2}
    INV-->>TX: Available = 10 - 2 = 8 (sufficient)

    Note over TX: Payment Intent Created
    TX->>INV: Reserve stock
    INV->>DB: UPDATE stock_reserved += quantity

    alt Payment SUCCESS
        TX->>INV: Deduct stock
        INV->>DB: stock -= quantity, stock_reserved -= quantity
    end

    alt Payment FAILED
        TX->>INV: Release reserved
        INV->>DB: stock_reserved -= quantity
    end
```
