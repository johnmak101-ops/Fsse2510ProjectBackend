# API Specification

## FSSE2510 E-Commerce Platform

| Item               | Detail                  |
|--------------------|-------------------------|
| **Document Version** | 1.1                   |
| **Project Name**     | FSSE2510 E-Commerce   |
| **Base URL**         | `/api` (or dynamically mapped) |
| **Authentication**   | `Authorization: Bearer <Firebase_JWT>` |

---

## 1. Public Endpoints (No Auth Required)

### 1.1 Products
Exposes product listing and details for guest browsing.
* `GET /public/products` - Filter/browse products (paginated, by minimum/maximum price, keyword/sort, category/tag).
* `GET /public/products/{idOrSlug}` - Get single product detail + promotions.
* `GET /public/products/categories` - List categories.
* `GET /public/products/collections` - List collections.
* `GET /public/products/tags` - List tags.
* `GET /public/products/category/{category}` - Paginated list isolated by category.
* `GET /public/products/collection/{collection}` - Paginated list isolated by collection.
* `GET /public/products/tag/{tag}` - Paginated list isolated by tag.

### 1.2 Checkout Utilities
* `GET /public/coupon/validate` (requires `?code=`) - Validates a discount code for the guest environment.
* `GET /public/promotions/active` - Lists all currently running automatic promotions.

### 1.3 CMS & Layout
* `GET /public/navigation/tree` - Returns the nested Navbar items hierarchy.
* `GET /public/showcase/collections` - Returns active homepage showcase banners/carousels.
* `GET /public/membership/configs` - Returns the defined membership rules & mechanics.

### 1.4 Stripe Webhook
* `POST /webhooks/stripe` - Validates the `Stripe-Signature` and updates corresponding Transaction entity.

---

## 2. Authenticated Endpoints (`Bearer Auth`)

### 2.1 User Profile (`/users`)
* `GET /users/me` - Profile overview including lifetime spend, tier details.
* `PATCH /users/profile` - Update profile data `{ "fullName", "phoneNumber", "address", "birthday" }`

### 2.2 Shipping Addresses (`/addresses`)
* `GET /addresses` - List user's saved addresses.
* `POST /addresses` - Create an address.
* `PUT /addresses/{id}` - Complete overwrite.
* `PATCH /addresses/{id}/default` - Set as primary default address.
* `DELETE /addresses/{id}` - Remove an address block.

### 2.3 Cart Management (`/cart`)
* `GET /cart` - Returns live cart, stock check, prices.
* `POST /cart/items/{sku}/{quantity}` - Add product variants.
* `PUT /cart/items/{sku}/{quantity}` - Overwrite item's specific quantity.
* `DELETE /cart/items/{sku}` - Drop variant from cart.
* `DELETE /cart/empty` - Nuke entire cart.

### 2.4 Transactions & Checkout (`/transactions`)
* `GET /transactions` - Auth user's order history.
* `GET /transactions/{id}` - Auth user's specific invoice.
* `POST /transactions/prepare` - Convert cart into a staging checkout (Calculates coupons + DB deductions).
* `PATCH /transactions/{id}/payment` - Initialize Stripe Payment Intent against the prepared ID.
* `PATCH /transactions/{transactionId}/success` - (Fallback webhook) marks transition to success.

### 2.5 Wishlist (`/api/wishlist`)
* `GET /api/wishlist` - Auth user's liked product list.
* `POST /api/wishlist/{pid}` - Toggle favorite on product id.
* `DELETE /api/wishlist/{pid}` - Remove favorite.

---

## 3. Admin Endpoints (`Bearer + ROLE_ADMIN`)

### 3.1 Product/Inventory Admin (`/admin/products`)
* `POST /admin/products` - Create product record.
* `PUT /admin/products/{productId}` - Overwrite/Update all basic fields.
* `DELETE /admin/products/{productId}` - De-list a product item.
* `POST /admin/products/{productId}/variants` - Setup SKU variables/stocks.
* `PATCH /admin/products/{productId}/variants/{sku}` - Sync real-time stocks.

### 3.2 Users Admin (`/admin/users`)
* `GET /admin/users` - Master list of registered users.
* `GET /admin/users/search` - Look up user by explicit FirebaseUid or Email.
* `POST /admin/users/set-role` - Grant root access to a specific account (`uid`, `role`).

### 3.3 Transaction Admin (`/admin/transactions`)
* `GET /admin/transactions` - Monitor order volume.
* `PATCH /admin/transactions/{id}/status` - Override transaction states manually (`PREPARE`, `SUCCESS`).

### 3.4 Showcase / CMS Admin (`/api/admin/showcase/collections`)
* `GET /api/admin/showcase/collections` - Overview of banner sequences.
* `POST /api/admin/showcase/collections` - Adds new block.
* `PUT /api/admin/showcase/collections/{id}` - Replaces banner block data.
* `DELETE /api/admin/showcase/collections/{id}` - Prune banner element.

### 3.5 Marketing Admin (`/admin/promotions`, `/admin/coupons`, `/admin/membership`)
* `POST /admin/promotions` - Mount new global/conditional promos.
* `PATCH /admin/promotions/{promoId}/assign/{pid}` - Bond an exclusive product to a promotional scope.
* `GET /admin/promotions`, `GET /admin/promotions/{id}` - Fetch live settings.
* `PUT /admin/promotions/{id}`, `DELETE /admin/promotions/{id}` - Alter mechanics.
* `GET /admin/coupons`, `POST /admin/coupons`, `DELETE /admin/coupons/{code}` - Maintain tracking keys for %/$$ off coupons.
* `GET /admin/membership/configs`, `POST /admin/membership/configs` - Control threshold mappings for tiers.

---

## 5. Standard Error DTO Example

```json
{
  "timestamp": "2026-03-18T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "ProductEntity with pid 9999 not found",
  "path": "/api/public/products/9999"
}
```
