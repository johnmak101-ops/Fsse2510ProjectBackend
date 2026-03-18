# API Contract — Gelato Pique E-Commerce

> **Version:** 2.0 | **Date:** 2026-03-18 | **Base URL:** `https://api.johnmak.store`

---

## Authentication

All non-public endpoints require a Firebase ID Token:
```
Authorization: Bearer <firebase-id-token>
```

---

## 1. Products (Public)

**Base:** `/public/products`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/public/products` | List products with pagination, filtering, sorting | None |
| GET | `/public/products/{id}` | Get product by ID | None |
| GET | `/public/products/slug/{slug}` | Get product by URL slug | None |
| GET | `/public/products/recommendations` | Get recommended products | None |
| GET | `/public/products/showcase` | Get showcase/featured products | None |
| GET | `/public/products/you-may-also-like` | Get "You May Also Like" suggestions | None |
| GET | `/public/products/showcase/collections` | Get showcase collection list | None |
| GET | `/public/products/search` | Full-text product search | None |
| GET | `/public/products/attributes` | Get available filter attributes | None |

**Query Parameters (Product List):**
- `page` (int) — Page number
- `size` (int) — Items per page
- `category` (string) — Filter by category slug
- `collection` (string) — Filter by collection slug
- `sort` (string) — Sort field (price_asc, price_desc, name, newest)
- `tag` (string) — Filter by tag
- `productType` (string) — Filter by product type

---

## 2. Products (Admin)

**Base:** `/products`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| POST | `/products` | Create a new product | Admin |
| PUT | `/products/{id}` | Full update a product | Admin |
| PATCH | `/products/{id}/metadata` | Update product metadata (badges, featured) | Admin |
| DELETE | `/products/{id}` | Delete a product | Admin |

---

## 3. Cart

**Base:** `/cart`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/cart` | Get user's cart items | User |
| POST | `/cart` | Add item to cart | User |
| PATCH | `/cart/{cid}` | Update cart item quantity | User |
| DELETE | `/cart/{cid}` | Remove item from cart | User |

---

## 4. Transactions (User)

**Base:** `/transactions`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/transactions` | List user's transactions | User |
| POST | `/transactions` | Create a new transaction | User |
| GET | `/transactions/{tid}` | Get transaction details | User |
| POST | `/transactions/{tid}/payment` | Prepare Stripe payment intent | User |
| PATCH | `/transactions/{tid}/success` | Mark transaction as completed | User |
| PATCH | `/transactions/{tid}/fail` | Abort/cancel transaction | User |
| PATCH | `/transactions/{tid}/processing` | Update status to PROCESSING | User |

**Create Transaction Request Body:**
```json
{
  "couponCode": "SUMMER2025",
  "usePoints": 100,
  "shippingAddressId": 3
}
```

---

## 5. Transactions (Admin)

**Base:** `/admin/transactions`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/admin/transactions` | List all transactions (paginated) | Admin |
| GET | `/admin/transactions/{tid}` | Get any transaction detail | Admin |
| PATCH | `/admin/transactions/{tid}/status` | Update transaction status | Admin |

---

## 6. Users

**Base:** `/users`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/users/me` | Get current user profile + membership info | User |
| PATCH | `/users/profile` | Update user profile (name, phone, birthday) | User |

---

## 7. Users (Admin)

**Base:** `/admin/users`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/admin/users` | List all users | Admin |
| GET | `/admin/users/search` | Search users by keyword | Admin |
| POST | `/admin/users/set-role` | Assign admin role to user | Admin |

---

## 8. Shipping Addresses

**Base:** `/addresses`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/addresses` | List user's shipping addresses | User |
| POST | `/addresses` | Create a new shipping address | User |
| PUT | `/addresses/{id}` | Update a shipping address | User |
| DELETE | `/addresses/{id}` | Delete a shipping address | User |
| PATCH | `/addresses/{id}/default` | Set address as default | User |

---

## 9. Wishlist

**Base:** `/wishlist`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/wishlist` | Get user's wishlist | User |
| POST | `/wishlist/{pid}` | Add product to wishlist | User |
| DELETE | `/wishlist/{pid}` | Remove product from wishlist | User |

---

## 10. Coupons (Public)

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/public/coupon/validate?code=XXX` | Validate a coupon code | User |

---

## 11. Coupons (Admin)

**Base:** `/admin/coupons`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/admin/coupons` | List all coupons | Admin |
| POST | `/admin/coupons` | Create a new coupon | Admin |
| PUT | `/admin/coupons/{code}` | Update a coupon | Admin |
| DELETE | `/admin/coupons/{code}` | Delete a coupon | Admin |

---

## 12. Promotions (Public)

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/public/promotions/active` | Get currently active promotions | None |

---

## 13. Promotions (Admin)

**Base:** `/admin/promotions`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/admin/promotions` | List all promotions | Admin |
| GET | `/admin/promotions/{id}` | Get promotion detail | Admin |
| POST | `/admin/promotions` | Create a new promotion | Admin |
| PUT | `/admin/promotions/{id}` | Update a promotion | Admin |
| DELETE | `/admin/promotions/{id}` | Delete a promotion | Admin |
| PATCH | `/admin/promotions/{promoId}/assign/{pid}` | Assign promotion to product | Admin |

---

## 14. Membership (Public)

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/public/membership` | Get membership tiers config | None |

---

## 15. Membership (Admin)

**Base:** `/admin/membership/configs`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/admin/membership/configs` | List all tier configs | Admin |
| PUT | `/admin/membership/configs/{level}` | Update a tier config | Admin |

---

## 16. Navigation

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/public/navigation` | Get navigation tree | None |
| GET | `/admin/navigation` | Get full navigation tree (incl. inactive) | Admin |
| PUT | `/admin/navigation` | Full replace navigation tree | Admin |

---

## 17. Showcase Collections (Admin)

**Base:** `/api/admin/showcase/collections`

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| GET | `/api/admin/showcase/collections` | List all showcase collections | Admin |
| POST | `/api/admin/showcase/collections` | Create showcase collection | Admin |
| PUT | `/api/admin/showcase/collections/{id}` | Update showcase collection | Admin |
| DELETE | `/api/admin/showcase/collections/{id}` | Delete showcase collection | Admin |

---

## 18. Stripe Webhook

| Method | Path | Description | Auth |
|:---|:---|:---|:---|
| POST | `/webhooks/stripe` | Handle Stripe payment events | Stripe Signature |

**Handled Events:**
- `payment_intent.succeeded` → Marks transaction as SUCCESS, deducts stock, awards points
- `payment_intent.payment_failed` → Marks transaction as FAILED, releases reserved stock

---

## Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Coupon code has expired",
  "timestamp": "2026-03-18T10:30:00"
}
```

| Code | Meaning |
|:---|:---|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid JWT) |
| 403 | Forbidden (insufficient role) |
| 404 | Not Found |
| 409 | Conflict (stock insufficient, coupon exhausted) |
| 500 | Internal Server Error |
