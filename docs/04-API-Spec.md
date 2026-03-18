# API Specification

## FSSE2510 E-Commerce Platform

| Item               | Detail                  |
|--------------------|-------------------------|
| **Document Version** | 1.0                   |
| **Project Name**     | FSSE2510 E-Commerce   |
| **Base URL**         | `/api` (e.g. `http://localhost:8080/api`) |
| **Authentication**   | `Authorization: Bearer <Firebase_JWT>` |

---

## 1. Public Endpoints (No Auth Required)

### 1.1 Products (`/public/product`)
Exposes product listing and details for guest browsing.

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `GET` | `/public/product` | Filter/browse products (paginated) | `page, minPrice, maxPrice, priceSort, dateSort, category, collection, tag` |
| `GET` | `/public/product/{idOrSlug}` | Get single product detail + active promotions | - |
| `GET` | `/public/product/categories` | List all predefined categories | - |
| `GET` | `/public/product/collections`| List all predefined collections| - |
| `GET` | `/public/product/tags` | List all predefined tags | - |
| `GET` | `/public/product/category/{category}` | Get products by category | `page` |
| `GET` | `/public/product/collection/{collection}` | Get products by collection | `page` |
| `GET` | `/public/product/tag/{tag}` | Get products by tag | `page` |

### 1.2 Checkout Utilities (`/public/coupon`, `/public/promotion`)

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `GET` | `/public/coupon/{code}` | Validates a coupon code | - |
| `GET` | `/public/promotion` | Lists all currently active promotions | - |

### 1.3 CMS & Layout (`/public/navigation`, `/public/showcase`, `/public/membership`)

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `GET` | `/public/navigation/tree` | Returns the nested Navbar items | - |
| `GET` | `/public/showcase` | Returns active homepage banners/carousels | - |
| `GET` | `/public/membership/tier` | Returns the defined membership tiers | - |

---

## 2. Authenticated Endpoints (`Bearer Auth`)

### 2.1 User Profile (`/user`)

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `GET` | `/user` | Get current user's profile | - |
| `PATCH`| `/user` | Update profile info | `{ "name", "phone" }` |
| `GET` | `/user/shippingaddress` | List saved shipping addresses | - |
| `POST` | `/user/shippingaddress` | Create a shipping address | `{ "building", "street", "district", "city", "country", "unit", "isDefault" }` |
| `PUT` | `/user/shippingaddress/{id}` | Update a shipping address | - |
| `DELETE`| `/user/shippingaddress/{id}` | Remove a shipping address | - |

### 2.2 Cart Management (`/cart`)

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `GET` | `/cart` | Get current cart with realtime pricing | - |
| `POST` | `/cart/{sku}/{quantity}` | Add a variant to the cart | - |
| `PATCH`| `/cart/{sku}/{quantity}` | Update quantity of a variant | - |
| `DELETE`| `/cart/{sku}` | Remove a variant from the cart | - |
| `DELETE`| `/cart/empty` | Empty the entire cart | - |

### 2.3 Transactions & Checkout (`/transaction`)

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `GET` | `/transaction` | Get authenticated user's order history | `page` |
| `GET` | `/transaction/{id}` | Get specific transaction details | - |
| `POST` | `/transaction/prepare` | Convert cart to PENDING transaction | `{ "shippingAddressId", "couponCode" }` |
| `PATCH`| `/transaction/{id}/pay` | Initialize Stripe Payment Intent | Returns `{ "clientSecret" }` |
| `PATCH`| `/transaction/{transactionId}/success` | Fallback manually set success (if webhook missed) | - |

### 2.4 Wishlist (`/api/wishlist`)

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `GET` | `/wishlist` | Get user's liked products | - |
| `POST` | `/wishlist/{productId}` | Toggle product in wishlist | - |

---

## 3. Admin Endpoints (`Bearer + ROLE_ADMIN`)

### 3.1 Product Admin (`/admin/product`)

| Method | Endpoint | Description | Query Params / Body |
|--------|----------|-------------|---------------------|
| `POST` | `/admin/product` | Create a new product | Full `ProductDetailsDto` |
| `PATCH`| `/admin/product/{productId}` | Update metadata / un-delete | - |
| `DELETE`| `/admin/product/{productId}` | Soft-delete product | - |
| `POST` | `/admin/product/{productId}/variant` | Add variant | `{ "sku", "price", "stock", "size" }` |
| `PATCH`| `/admin/product/{productId}/variant/{sku}` | Update variant price/stock | - |

### 3.2 CMS Admin

| Method | Endpoint | Description | Target Mod |
|--------|----------|-------------|------------|
| `POST` | `/admin/navigation/item` | Create a Navbar link | Navigation |
| `POST` | `/admin/navigation/init` | Seed default layout | Navigation |
| `POST` | `/admin/showcase` | Create a homepage banner | Showcase |
| `DELETE`| `/admin/showcase/{id}` | Remove banner | Showcase |

### 3.3 Marketing Admin

| Method | Endpoint | Description | Group |
|--------|----------|-------------|-------|
| `POST` | `/admin/coupon` | Create promo code | Coupons |
| `DELETE`| `/admin/coupon/{code}` | Delete promo code | Coupons |
| `POST` | `/admin/promotion` | Create active promotion rules | Promotions |
| `POST` | `/admin/promotion/{promotionId}/products/{productId}` | Link promotion to product | Promotions |
| `GET` | `/admin/membership/tier` | List tier rules | General Settings |

### 3.4 User & Order Management

| Method | Endpoint | Description | Group |
|--------|----------|-------------|-------|
| `GET` | `/admin/user` | List all users (paginated) | Users |
| `GET` | `/admin/transaction` | List all orders (paginated) | Orders |
| `PATCH`| `/admin/transaction/{id}/status` | Manually update order status | Orders |
| `PATCH`| `/admin/user/{uid}/admin` | Grant a user `ROLE_ADMIN` | Users |

---

## 4. Webhook Endpoints

| Method | Endpoint | Source | Description |
|--------|----------|--------|-------------|
| `POST` | `/webhook/stripe` | Stripe API | Triggered on *checkout.session.completed* or *payment_intent.succeeded*. Secures with `Stripe-Signature` and raw body validation. Updates local Transaction status to `SUCCESS`. |

---

## 5. DTO Standard Conventions

Success Response (`200 OK`):
```json
{
  "cartItems": [
    {
      "sku": "A1B2",
      "name": "Testing Product",
      "price": 100.00,
      "quantity": 2,
      "subtotal": 200.00
    }
  ],
  "total": 200.00
}
```

Error Response (`4xx / 5xx`):
```json
{
  "timestamp": "2026-03-18T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock not enough for item: A1B2, Stock available: 1",
  "path": "/api/cart/A1B2/2"
}
```
