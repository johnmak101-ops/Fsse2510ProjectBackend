# Business Requirements Document (BRD)

## FSSE2510 E-Commerce Platform

| Item               | Detail                  |
|--------------------|-------------------------|
| **Document Version** | 1.0                   |
| **Project Name**     | FSSE2510 E-Commerce   |
| **Technology Stack** | Spring Boot 3.5 / Java 21, Next.js 16.1.6 / TypeScript |
| **Database**         | MySQL + Redis Cache   |
| **Authentication**   | Firebase JWT + Spring Security |
| **Payment Gateway**  | Stripe                |

---

## 1. Executive Summary

FSSE2510 is a full-stack e-commerce platform that provides a complete online shopping experience for customers and a comprehensive management dashboard for administrators. The platform supports product browsing, cart management, coupon/promotion systems, a tiered membership loyalty programme, Stripe-powered checkout, and an admin-controlled CMS for navigation and homepage showcases.

---

## 2. Stakeholders

| Role         | Description                                                    |
|--------------|----------------------------------------------------------------|
| **Customer** | End-user who browses products, adds to cart, and makes purchases. |
| **Admin**    | Back-office user who manages products, promotions, orders, users, navigation, and CMS content. |
| **System**   | Automated background jobs (schedulers) that maintain data integrity. |

---

## 3. Business Objectives

| ID    | Objective                                                                 |
|-------|---------------------------------------------------------------------------|
| BO-01 | Provide a seamless online shopping experience from browsing to checkout.  |
| BO-02 | Enable a flexible promotion and coupon engine for marketing campaigns.    |
| BO-03 | Implement a tiered membership loyalty programme to reward repeat customers. |
| BO-04 | Allow admins to fully control the product catalogue, pricing, and inventory. |
| BO-05 | Provide admin-controlled CMS for site navigation (Navbar) and homepage showcases. |
| BO-06 | Integrate a secure, PCI-compliant payment flow via Stripe.                |
| BO-07 | Automate stale transaction cleanup and expired promotion removal via schedulers. |
| BO-08 | Support user profile management, shipping addresses, and wishlists.       |

---

## 4. Functional Requirements

### 4.1 Product Management

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-01 | Customers can browse all active products (paginated).                    |
| FR-02 | Customers can view product details by ID or URL slug.                    |
| FR-03 | Customers can browse products using the navigation menu categories/collections (Text search by name is restricted to Admins). 
| FR-04 | System recommends related products.                                       |
| FR-05 | Admins can create, update (full & metadata), and soft-delete products.   |
| FR-05b| Admins can search and filter the product catalog by name, category, or other attributes in the Dashboard. |
| FR-06 | Each product has inventory variants (SKU, size, colour, stock, weight).  |
| FR-07 | Products support multiple images, tags, categories, and collections.     |

### 4.2 Shopping Cart

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-08 | Authenticated users can add items to their cart by SKU.                   |
| FR-09 | Users can update item quantity or remove items from the cart.             |
| FR-10 | Cart displays real-time pricing and subtotals.                            |
| FR-11 | Stock is validated before adding to cart (reserved stock management).     |

### 4.3 Checkout & Transactions

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-12 | Users can create a transaction from their cart items.                     |
| FR-13 | System creates a Stripe Payment Intent for secure payment.               |
| FR-14 | Transaction states: `PENDING → PROCESSING → SUCCESS / FAILED / ABORTED`.|
| FR-15 | Successful payment triggers stock deduction, membership point accrual, and spending cycle update. |
| FR-16 | Users can view their transaction history (paginated).                     |
| FR-17 | Transaction stores a snapshot of the shipping address at time of order.   |
| FR-17b| **Exception Flow (Payment Failure)**: If payment fails, the transaction is marked as `FAILED`. User is guided to an error page with a clear "Retry Payment" Call-To-Action. |
| FR-17c| **Exception Flow (Refund)**: Admin initiates refund via Stripe Dashboard. System listens to `charge.refunded` webhook, updates transaction to `REFUNDED`. Admin manually manages inventory/points rollback if necessary. |

### 4.4 Coupon System

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-18 | Admins can create coupons with codes, discount type (% / fixed), min spend, expiry, and usage limits. |
| FR-19 | Coupons can be restricted to specific membership tiers.                   |
| FR-20 | Customers can validate a coupon code at checkout (public endpoint).       |
| FR-21 | Admins can list valid coupons, update, and delete them.                   |

### 4.5 Promotion Engine

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-22 | Admins can create promotions with 10 types: item-level discounts, BOGO, order-level discounts, membership-based, and storewide sales. |
| FR-23 | Promotions target specific products, categories, collections, or tags.   |
| FR-24 | Promotions have start/end dates and optional membership tier requirements.|
| FR-25 | Active promotions are exposed via public API for frontend upsell banners.|
| FR-26 | Admins can assign promotions to specific products.                       |

### 4.6 Membership & Loyalty

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-27 | Five membership tiers: `NO_MEMBERSHIP → BRONZE → SILVER → GOLD → DIAMOND`. |
| FR-28 | Each tier is configurable: min spend threshold, point earn rate, validity, and grace period. |
| FR-29 | Users accumulate spending within a cycle; tier upgrades/downgrades happen based on thresholds. |
| FR-30 | Users earn redeemable points automatically on successful purchases (Formula: `Amount Spent * 10 * Point Rate`). |
| FR-30b| Users can redeem points at checkout for a cash discount (Default: `10 Points = HK$1.00`). |
| FR-31 | Membership tier info is publicly accessible.                             |
| FR-32 | Admins can update membership tier configurations and the global points redemption rate. |

### 4.7 User Management

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-33 | First-time login (Google SSO or Email/Password) auto-creates a user record from Firebase JWT claims.    |
| FR-34 | Users can view and update their profile (name, phone, etc.).             |
| FR-35 | Admins can list all users, search users, and set user roles (ADMIN).     |
| FR-36 | Role assignment via Firebase custom claims (user must re-login).         |

### 4.8 Shipping Addresses

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-37 | Users can create, update, and delete shipping addresses.                 |
| FR-38 | Users can set a default shipping address.                                |
| FR-39 | Address is snapshotted into the transaction at checkout time.             |

### 4.9 Wishlist

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-40 | Authenticated users can add/remove products to/from their wishlist.      |
| FR-41 | Users can view their full wishlist.                                       |

### 4.10 Navigation CMS (Admin-Controlled Navbar)

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-42 | Admins can create, update, delete, and reorder navigation items.         |
| FR-43 | Navigation supports hierarchical structure (parent → children).          |
| FR-44 | Items have types (`TAB`, `DROPDOWN_ITEM`) and actions (filter by category/collection/tag, URL link). |
| FR-45 | Items can be flagged as "new" and toggled active/inactive.               |
| FR-46 | Public endpoint exposes the full navigation tree for the frontend Navbar.|
| FR-47 | Admins can initialise default navigation from a template.                |

### 4.11 Homepage Showcase CMS

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-48 | Admins can create, update, and delete showcase collections.              |
| FR-49 | Showcase collections have a title, image, tag-based product query, display order, and active status. |
| FR-50 | Public endpoint exposes active showcase collections for the homepage.     |

### 4.12 Scheduled Background Jobs

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-51 | **Promotion Scheduler**: runs daily at 02:00 to clear expired promotions from products. |
| FR-52 | **Stale Transaction Scheduler (Processing)**: runs every 30 min to reconcile `PROCESSING` transactions older than 2 hours against Stripe. |
| FR-53 | **Stale Transaction Scheduler (Pending)**: runs every 15 min to abort `PENDING` transactions older than 30 min, recovering reserved stock into cart items. |

### 4.13 Stripe Webhook Integration

| ID    | Requirement                                                               |
|-------|---------------------------------------------------------------------------|
| FR-54 | System receives Stripe webhook events (e.g. `checkout.session.completed`).|
| FR-55 | Webhook verifies Stripe signature header for security.                   |
| FR-56 | On successful payment event, system updates transaction status to `SUCCESS`. |

---

## 5. Non-Functional Requirements

| ID     | Requirement                                                              |
|--------|--------------------------------------------------------------------------|
| NFR-01 | **Security**: Firebase JWT authentication; role-based access control (`ROLE_ADMIN`). |
| NFR-02 | **Security**: CORS configured to allow only the designated frontend URL. |
| NFR-03 | **Security**: Public endpoints (`/public/**`, `/webhooks/**`) are unauthenticated; all others require JWT. |
| NFR-04 | **Performance**: Redis caching for frequently accessed data. Homepage and product listing load time must be < 2 seconds (95th percentile). |
| NFR-05 | **Data Integrity**: Reserved stock mechanism prevents overselling.       |
| NFR-06 | **Scalability**: Paginated responses for products, transactions, and user lists. |
| NFR-07 | **Reliability**: Scheduled reconciliation of stale transactions against Stripe. |
| NFR-08 | **Maintainability**: Layered architecture (Controller → Service → Repository → Entity) with MapStruct DTO mapping. |

---

## 6. System Architecture Overview

```
┌─────────────┐     Firebase JWT     ┌─────────────────────┐
│  Next.js 16.1.6 UI  │ ──────────────────→ │  Spring Boot API     │
│  (Frontend)  │ ←────────────────── │  (Backend)           │
└─────────────┘                      │                     │
                                     │  ┌───────────┐      │
                                     │  │ Security   │      │
                                     │  │ Filter     │      │
                                     │  └─────┬─────┘      │
                                     │        ↓            │
                                     │  ┌───────────┐      │
                                     │  │Controllers │      │
                                     │  └─────┬─────┘      │
                                     │        ↓            │
                                     │  ┌───────────┐      │
                                     │  │ Services   │      │
                                     │  └─────┬─────┘      │
                                     │        ↓            │
                                     │  ┌───────────┐      │
                                     │  │Repositories│      │
                                     │  └─────┬─────┘      │
                                     │        ↓            │
                                     │  ┌───────────┐      │
                                     │  │  MySQL DB  │      │
                                     │  └───────────┘      │
                                     │                     │
                                     │  ┌───────────┐      │
                                     │  │Redis Cache │      │
                                     │  └───────────┘      │
                                     │                     │
                                     │  ┌───────────┐      │
                                     │  │ Schedulers │      │
                                     │  └───────────┘      │
                                     └─────────────────────┘
                                              ↕
                                     ┌─────────────────────┐
                                     │    Stripe API        │
                                     │  (Payment Gateway)   │
                                     └─────────────────────┘
```

---

## 7. Glossary

| Term                  | Definition                                                          |
|-----------------------|---------------------------------------------------------------------|
| **SKU**               | Stock Keeping Unit — unique identifier for a product variant.       |
| **Payment Intent**    | Stripe object representing a customer's intent to pay.              |
| **Webhook**           | HTTP callback from Stripe notifying payment status changes.         |
| **Reserved Stock**    | Inventory temporarily held during checkout to prevent overselling.  |
| **Spending Cycle**    | Period during which a user's total spending determines their membership tier. |
| **Grace Period**      | Days a user retains their tier after a spending cycle ends.         |
| **Showcase Collection** | Admin-curated product group displayed on the homepage.           |
| **Navigation Item**   | CMS-managed menu entry for the site navbar.                        |

---

## 8. Data Dictionary (Key Financial Fields)

To ensure financial accuracy and data integrity across the platform, the following data types and precision rules apply:

| Entity / Field | Data Type | Precision / Rule | Description |
|---|---|---|---|
| `Product.price` | `DECIMAL(10,2)` | 2 decimal places | Standard product pricing. Max value `99,999,999.99`. |
| `Transaction.total` | `DECIMAL(10,2)` | 2 decimal places | Final order total accurately calculated and passed to Stripe. |
| `User.accumulatedSpending` | `DECIMAL(10,2)` | 2 decimal places | Tracked spending for membership tier evaluation. |
| `User.points` | `DECIMAL(10,2)` | 2 decimal places | Loyalty points balance. Rounding strategy: Standard round to 2 decimal places during accrual/points-to-cash redemption. |
| `Coupon.discountValue` | `DECIMAL(10,2)` | 2 decimal places | For fixed value discounts, represents local currency. For `%` discounts, represents percentage. |
| `Promotion.discountValue` | `DECIMAL(10,2)` | 2 decimal places | Target discount amount for promotion rules. |
