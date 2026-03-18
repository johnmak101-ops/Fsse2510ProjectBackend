# Business Logic & Architectural Decisions — Backend

> **Version:** 2.0 | **Date:** 2026-03-18

---

## Decision Log

### BLD-001: Transaction Product Snapshot (Not FK Reference)
- **Decision**: `transaction_product` stores product snapshots (name, price, image, size, color) rather than FK to product table
- **Context**: Using FK would break historical order data integrity when products are modified/deleted
- **Trade-off**: Data redundancy vs historical accuracy
- **Result**: Historical orders always preserve correct prices and product info at time of purchase

### BLD-002: Two-Step Fetch Pattern (N+1 Prevention)
- **Decision**: JPA Repository first queries `Slice<Integer>` IDs, then shallow fetches by ID list
- **Context**: Traditional `JOIN FETCH` causes memory bloat in pagination; Lazy Loading causes N+1
- **Trade-off**: Two queries vs one large JOIN
- **Result**: Controlled memory usage with stable query performance

### BLD-003: Firebase Auth + Spring Security (Stateless JWT)
- **Decision**: Firebase Auth as Identity Provider, Spring Security for JWT validation
- **Context**: Building custom auth carries high security risk; Firebase provides mature Google SSO + secure token management
- **Trade-off**: External service dependency vs security + development speed
- **Result**: Zero password management risk, stateless architecture enables horizontal scaling

### BLD-004: RBAC via Environment Variable (ADMIN_EMAILS)
- **Decision**: Admin role assigned via `ADMIN_EMAILS` environment variable
- **Context**: Course project scope does not require a full role management UI
- **Trade-off**: Flexibility vs simplicity
- **Result**: Configure at deployment time with no extra DB tables or UI needed

### BLD-005: Coupon Code as Primary Key
- **Decision**: `coupon` table uses `code` string as PK
- **Context**: Coupon code is already a unique identifier; no need for auto-increment ID
- **Trade-off**: String PK slightly slower vs more intuitive business semantics
- **Result**: Simplified redemption flow; API uses coupon code directly as path parameter

### BLD-006: Optimistic Locking on Transaction
- **Decision**: Transaction table uses `@Version` for optimistic locking
- **Context**: Same order may be updated simultaneously by webhook and frontend
- **Trade-off**: Rare retry needed vs race condition prevention
- **Result**: Effectively prevents phantom charges and duplicate stock deduction

### BLD-010: Redis Cache for Products
- **Decision**: Product reads cached via `@Cacheable` to Redis
- **Context**: Product API is the highest-frequency read operation; needs MySQL protection
- **Trade-off**: Cache invalidation complexity vs performance gain
- **Result**: Admin updates evict cache; read performance significantly improved

### BLD-011: Google Jib for Container Building
- **Decision**: Google Jib replaces Dockerfile for container image building
- **Context**: Traditional Dockerfile requires Docker daemon; Jib builds directly from Gradle
- **Trade-off**: Flexibility (no Dockerfile) vs build speed + CI/CD simplification
- **Result**: CI/CD pipeline needs no Docker-in-Docker, faster builds

### BLD-012: Promotion Multi-Target Architecture
- **Decision**: Four `@ElementCollection` tables (pids, categories, collections, tags) for multi-dimensional targeting
- **Context**: Normal FK only supports one relationship type; promotions need flexible multi-target setup
- **Trade-off**: More join tables vs flexibility
- **Result**: A single Promotion can target specific products + entire categories + specific tags simultaneously

### BLD-013: Shipping Address Snapshot on Transaction
- **Decision**: Transaction table embeds recipient address fields as snapshots
- **Context**: Using only FK, user deleting addresses would lose order delivery info
- **Trade-off**: Data redundancy vs order completeness
- **Result**: Even if address book is modified/cleared, historical orders retain correct recipient and address info
