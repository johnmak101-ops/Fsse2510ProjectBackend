# Functional Requirements — Backend

> **Version:** 2.0 | **Date:** 2026-03-18

---

## FR-01: User Authentication & Authorization

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-01.1 | System shall validate Firebase JWT tokens on every authenticated request | Must |
| FR-01.2 | System shall auto-create a UserEntity on first login (Firebase UID lookup) | Must |
| FR-01.3 | System shall assign ROLE_ADMIN if user email is in `ADMIN_EMAILS` environment variable | Must |
| FR-01.4 | System shall return 401 for expired/invalid JWTs | Must |
| FR-01.5 | System shall return 403 for insufficient role access | Must |

### Acceptance Criteria
```gherkin
Scenario: First-time Google login creates new user
  Given a user signs in via Google for the first time
  When the backend receives a valid Firebase JWT
  Then a new UserEntity is created with NO_MEMBERSHIP level
  And the user profile is returned with isInfoComplete = false
```

---

## FR-02: Product Catalog

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-02.1 | System shall support paginated product listing with cursor-based pagination | Must |
| FR-02.2 | System shall support filtering by category, collection, tag, and product type | Must |
| FR-02.3 | System shall support sorting by price (asc/desc), name, and newest | Must |
| FR-02.4 | System shall return product details including images, inventory, and promotions | Must |
| FR-02.5 | System shall provide a full-text search endpoint | Should |
| FR-02.6 | System shall return available filter attributes via `/public/products/attributes` | Must |
| FR-02.7 | System shall cache product reads in Redis (`@Cacheable`) | Must |
| FR-02.8 | System shall provide product recommendations and "You May Also Like" | Should |

### Acceptance Criteria
```gherkin
Scenario: Paginated product list with filters
  Given there are 50 products in category "tops"
  When a GET request is made to /public/products?category=tops&page=0&size=12
  Then 12 products are returned with hasNext = true
  And all products belong to the "tops" category
```

---

## FR-03: Shopping Cart

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-03.1 | System shall persist cart items per user (server-side) | Must |
| FR-03.2 | System shall use SKU as cart item key (not product ID) | Must |
| FR-03.3 | System shall increment quantity if same SKU already in cart | Must |
| FR-03.4 | System shall allow updating cart item quantity | Must |
| FR-03.5 | System shall allow removing individual cart items | Must |

### Acceptance Criteria
```gherkin
Scenario: Add same SKU twice increments quantity
  Given user has SKU "GP-PAJAMA-M-PINK" with quantity 1 in cart
  When user adds SKU "GP-PAJAMA-M-PINK" with quantity 2
  Then cart item quantity becomes 3
```

---

## FR-04: Checkout & Payment

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-04.1 | System shall create a transaction from cart items with PREPARE status | Must |
| FR-04.2 | System shall validate stock availability before transaction creation | Must |
| FR-04.3 | System shall snapshot product details into `transaction_product` | Must |
| FR-04.4 | System shall snapshot shipping address into transaction record | Must |
| FR-04.5 | System shall support coupon code application with validation rules | Must |
| FR-04.6 | System shall support member points redemption (1 point = $1) | Must |
| FR-04.7 | System shall create Stripe PaymentIntent and return clientSecret | Must |
| FR-04.8 | System shall transition status: PREPARE → PENDING → PROCESSING → SUCCESS/FAILED | Must |
| FR-04.9 | System shall handle Stripe webhooks for payment confirmation | Must |
| FR-04.10 | System shall deduct stock and award points on SUCCESS | Must |
| FR-04.11 | System shall release reserved stock on FAILED | Must |

### Discount Stacking Logic
```
1. subtotal = SUM(item.price × item.quantity)
2. couponDiscount = applyCoupon(subtotal, couponCode)
3. afterCoupon = subtotal - couponDiscount
4. pointsDiscount = MIN(usedPoints, afterCoupon)
5. finalTotal = MAX(afterCoupon - pointsDiscount, 0)
```

### Acceptance Criteria
```gherkin
Scenario: Checkout with coupon and points
  Given cart total is $200, coupon "SAVE20" gives 20% off, user has 500 points
  When user applies coupon and uses 30 points
  Then order total = $200 - $40 (coupon) - $30 (points) = $130

Scenario: Insufficient stock prevents checkout
  Given Product A has 1 unit in stock
  When user tries to checkout with quantity 2
  Then the system returns 409 Conflict with "Insufficient stock"

Scenario: Webhook confirms payment
  Given Stripe sends payment_intent.succeeded for transaction #123
  Then transaction #123 status becomes SUCCESS
  And stock is deducted and member points are awarded
```

---

## FR-05: Membership & Loyalty

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-05.1 | System shall maintain 5 membership levels: NO_MEMBERSHIP → BRONZE → SILVER → GOLD → DIAMOND | Must |
| FR-05.2 | System shall auto-upgrade tier when accumulated spending exceeds threshold | Must |
| FR-05.3 | System shall award points per order based on tier's `point_rate` | Must |
| FR-05.4 | System shall track cycle spending and enforce grace period on tier downgrade | Should |
| FR-05.5 | System shall allow admin to configure tier thresholds via API | Must |

---

## FR-06: Coupon Management (Admin)

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-06.1 | Admin shall create coupons with code, discount type/value, validity, and usage limits | Must |
| FR-06.2 | Admin shall set minimum spend and required membership tier for coupons | Should |
| FR-06.3 | System shall validate coupon: active, not expired, usage limit, tier requirement | Must |
| FR-06.4 | System shall auto-increment usage_count on successful redemption | Must |

---

## FR-07: Promotion Management (Admin)

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-07.1 | Admin shall create promotions with type, discount, and date range | Must |
| FR-07.2 | Admin shall target promotions to specific products, categories, collections, or tags | Must |
| FR-07.3 | Admin shall assign/unassign promotions to individual products | Must |
| FR-07.4 | System shall only return active promotions (within date range) on public endpoint | Must |

---

## FR-08: Wishlist

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-08.1 | User shall add/remove products from wishlist (toggle pattern) | Must |
| FR-08.2 | System shall enforce one wishlist entry per user per product | Must |

---

## FR-09: Shipping Address

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-09.1 | User shall manage multiple shipping addresses (CRUD) | Must |
| FR-09.2 | User shall set one address as default | Must |
| FR-09.3 | Setting a new default shall unset the previous default | Must |

---

## FR-10: Admin Operations

| ID | Requirement | Priority |
|:---|:---|:---|
| FR-10.1 | Admin shall manage products (CRUD + metadata updates) | Must |
| FR-10.2 | Admin shall view and search all users | Must |
| FR-10.3 | Admin shall view all transactions and update transaction status | Must |
| FR-10.4 | Admin shall manage showcase collections and navigation items | Should |
