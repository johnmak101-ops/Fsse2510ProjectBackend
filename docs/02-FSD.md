# Functional Specification Document (FSD)

## FSSE2510 E-Commerce Platform

| Item               | Detail                  |
|--------------------|-------------------------|
| **Document Version** | 1.0                   |
| **Project Name**     | FSSE2510 E-Commerce   |

---

## 1. Introduction
This FSD details the system architecture, domain model, and functional modules of the FSSE2510 E-Commerce backend API. It serves as the blueprint for the frontend implementation and technical reference.

---

## 2. System Architecture

The backend is built as a monolithic Spring Boot REST API providing JSON responses. It follows a layered architecture:

1. **Controller Layer**: Handles HTTP requests, input validation, and maps domain entities to Data Transfer Objects (DTOs) via MapStruct.
2. **Service Layer**: Contains core business logic, transactional boundaries, and external API integrations (Stripe).
3. **Repository Layer**: Interfaces with the MySQL database using Spring Data JPA.
4. **Security Filter Chain**: Validates Firebase JWT tokens and enforces Role-Based Access Control (RBAC).
5. **Scheduled Jobs**: Background tasks for system maintenance (stock recovery, promotion expiry).

### 2.1 Caching Strategy
Redisson (Redis) is used to cache read-heavy endpoints:
- **Public Navigation**: Cached to reduce database load on the homepage.
- **Product Details**: Frequently accessed product data is cached.
- **Showcase Collections**: Cached for fast homepage rendering.

### 2.2 Payment Gateway Data Flow
1. Frontend calls `/transaction/prepare` with a cart.
2. Backend creates a `Transaction` in `PENDING` state and initializes a Stripe `PaymentIntent`.
3. Backend returns the `client_secret` to frontend.
4. Frontend completes the payment securely via Stripe Elements.
5. Stripe sends a webhook to `/webhook/stripe`.
6. Backend verifies signature, updates transaction to `SUCCESS`, triggers membership updates, and deducts actual stock.

---

## 3. Domain Model (Entity Schema)

### 3.1 Core E-Commerce
* **Product**: The core item. Has a name, description, SEO slug, categories, tags, and collections.
* **ProductVariant**: Defines specific iterations (SKU) of a Product. Has size, colour, stock level, reserved stock, and price overrides.
* **Category / Collection / Tag**: Taxonomy enums/entities used for filtering products.

### 3.2 User & Profile
* **User**: Auths via Firebase UID. Stores name, phone, email, and roles (USER/ADMIN).
* **ShippingAddress**: Bound to a User. Contains unit, building, street, district, city, country. Supports a `isDefault` flag.
* **Wishlist**: A many-to-many relationship between User and Product.

### 3.3 Cart & Checkout
* **CartItem**: Bound to a User and a ProductVariant. Stores quantity.
* **Transaction**: The order record. Stores status (`PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`), total amount, applied discounts, Stripe PaymentIntent ID, and shipping snapshot.
* **TransactionItem**: Immutable snapshot of the ProductVariant at the time of purchase.

### 3.4 Marketing & Promotions
* **Coupon**: Code-based discount. Configurable with value type (fixed/percentage), minimum spend, global usage quota, and expiry dates.
* **Promotion**: System-applied discount. Can target specific products, categories, or the entire store. 
  - **Types**: `PERCENTAGE_OFF` (e.g. 10% off), `FIXED_AMOUNT_OFF` (e.g. -$50), `BOGO` (Buy One Get One Free), `BUNDLE_DISCOUNT` (e.g. Buy 3 for $100).
  - **Stacking Configuration**: Determines if a promotion can be stacked with other promotions or coupons.

### 3.5 Loyalty Programme
* **MembershipTier**: Defines criteria (min spend required) and benefits (point multiplier). Tiers: NONE, BRONZE, SILVER, GOLD, DIAMOND.
* **SpendingCycle**: Tracks a user's accumulated spending within a defined timeframe (e.g. 1 year) to determine upgrade/downgrade logic.
* **Grace Period**: After a cycle expires, users have a configurable grace period (e.g., 30 days) to retain their tier benefits while their new cycle starts.

### 3.6 CMS Configs
* **NavigationItem**: Represents a Navbar node. Can be nested (parent/child). Points to URLs or product filters.
* **ShowcaseCollection**: Homepage banner/carousel config. Links a banner image and title to a specific Product Tag.

---

## 4. Module Specifications

### 4.1 Authentication Module
* **Supported Providers**: Frontend implements Firebase Auth for **Google Sign-In** and **Email/Password**. The backend validates the unified JWT.
* **JWT Validation**: All `/user/**`, `/admin/**`, `/cart/**`, `/transaction/**` endpoints require a valid Bearer token from Firebase.
* **Role Enforcement**: Endpoints under `/admin/**` require the `ROLE_ADMIN` authority.
* **User Provisioning**: If a valid JWT is received but the user does not exist in the DB, the system auto-creates a User record using claims (email, UID).

### 4.2 Product Discovery Module (Public)
* **Listing**: `/public/product` provides paginated results with extensive filtering (price min/max, category, collection, tag, sort by price/date).
* **Details**: `/public/product/{idOrSlug}` returns comprehensive details including all variants and their current stock logic. Active promotions linked to the product are included in the DTO.

### 4.3 Stock Management Module
* **Reserved Stock**: When a user prepares a checkout, the requested quantity is moved from `stock` to `reservedStock`.
* **Deduction**: Upon successful payment via Stripe webhook, `reservedStock` is deducted.
* **Recovery**: If a transaction sits in `PENDING` for >30 minutes, the Stale Transaction Scheduler aborts it and moves `reservedStock` back to available `stock`.

### 4.4 Checkout & Pricing Engine
* **Cart Subtotal**: Calculated dynamically based on the current price of variants in the cart.
* **Coupon Validation**: Validates if the user meets the membership tier requirement, minimum spend, and quota limits before allowing application.
* **Promotion Application**: (Admin logic) Overrides product prices or calculates cart-level discounts based on complex matrix rules (e.g. BOGO).
* **Stacking Logic & Conflicts**:
  - The system checks `isStackable` flags.
  - If multiple promotions apply to the same item, the system applies the mathematically *best discount for the user*, discarding duplicate or weaker promotions on that item unless explicitly stackable.
  - Coupons and Promotions are mutually exclusive by default unless `canStackWithCoupon` is true.

### 4.5 Membership Lifecycle Module
* **Default Tier Configurations** (Admin Configurable):
  * **NO MEMBERSHIP**: Min. Spend: Free Entry | Point Rate: 0% | Validity: LIFETIME | Grace Period: N/A
  * **BRONZE**: Min. Spend: HK$100 | Point Rate: 1% | Validity: LIFETIME | Retention: Permanent | Grace Period: N/A
  * **SILVER**: Min. Spend: HK$150 | Point Rate: 3% | Validity: 1 YEAR | Retention: HK$150 Annual Spend | Grace Period: 90 Days
  * **GOLD**: Min. Spend: HK$500 | Point Rate: 5% | Validity: 1 YEAR | Retention: HK$500 Annual Spend | Grace Period: 90 Days
  * **DIAMOND**: Min. Spend: HK$20,000 | Point Rate: 7% | Validity: 1 YEAR | Retention: HK$20,000 Annual Spend | Grace Period: 90 Days

* **Cycle Tracking**: Every user has an active `SpendingCycle` (12 months for Silver+).
* **Point Accrual**: Successful transactions yield points automatically based on the tier rate. **Formula**: `HK$1 = 10 * Point Rate` (e.g., At Gold tier 5%, HK$100 spent = 1,000 * 5% = 50 Points).
* **Points Redemption**: Points translate directly into cash savings at checkout. **Redemption Value**: `10 Points = HK$1.00`. The base point rate, min spending, and redemption rate are dynamic and controlled by the Admin. The discount cannot exceed the cart subtotal, preventing negative numbers. Shipping fees do not earn points and cannot be discounted by points.
* **Tier Evaluation**: After a successful payment, system evaluates cycle spending based on the configured values. If it crosses the next threshold, instant upgrade.
* **Downgrades & Grace Period**: If a cycle expires and goals aren't met, the tier is not instantly revoked. A `Grace Period` (e.g. 90 days) is activated where the user keeps the old tier perks but spending counts towards the *new* cycle. If the new cycle doesn't meet the threshold by the end of the Grace Period, the tier drops.

### 4.6 Navigation CMS Module
* **Tree Generation**: The database stores flat records with `parentId`. The backend algorithm recursively constructs a nested JSON tree for the frontend `/public/nav` endpoint.
* **Template Initialization**: Admins can seed the database with a predefined UI layout.

---

## 5. Automated Schedulers

| Scheduler Job | Frequency | Purpose |
|---------------|-----------|---------|
| `syncExpiredPromotions` | Daily @ 02:00 | Finds promotions past `endDate` and unlinks them from active products. |
| `abortStalePendingTransactions` | Every 15 mins | Identifies `PENDING` transactions older than 30 mins, sets them to `ABORTED`, and rolls back reserved stock. |
| `reconcileProcessingTransactions` | Every 30 mins | Polls Stripe API to verify the status of `PROCESSING` transactions older than 2 hours. If Stripe says 'succeeded', forces local status to `SUCCESS`. |

---

## 6. Error Handling Strategy

The system uses a `@ControllerAdvice` global exception handler mapping custom exceptions to standard HTTP codes:
* `400 Bad Request`: `InsufficientStockException`, `InvalidCouponException`
* `401 Unauthorized`: Invalid JWT token, Firebase auth errors.
* `403 Forbidden`: User attempting to access Admin endpoints without `ROLE_ADMIN`.
* `404 Not Found`: `ProductNotFoundException`, `ResourceNotFoundException`.
* `409 Conflict`: `DuplicateUserException`, `ConcurrencyStockException`.

*All error responses conform to a standard JSON format containing `timestamp`, `status`, `error`, `message`, and `path`.*
