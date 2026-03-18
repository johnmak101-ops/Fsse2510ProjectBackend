# Definition of Done (DoD)

## FSSE2510 E-Commerce Platform

| Item               | Detail                  |
|--------------------|-------------------------|
| **Document Version** | 1.0                   |
| **Project Name**     | FSSE2510 E-Commerce   |

---

## 1. Introduction
The FSSE2510 E-Commerce Backend is a **One-Man Project**. The Definition of Done (DoD) is tailored for pragmatic, rapid execution while maintaining essential quality. It establishes the baseline standards that a feature should meet (like basic logic testing and manual verification) without the heavy bureaucracy of enterprise-level constraints (e.g. strict 80% coverage enforcement is NOT required).

---

## 2. General Development DoD
Before a feature is considered complete and committed to the `main` branch, the following conditions should be met:

- [ ] **Self-Review**: Code is self-reviewed for logic correctness, defensive coding, and edge cases.
- [ ] **Configuration**: No hardcoded secrets; use `application.yml` or `.env`.
- [ ] **Clean Code**: Follows Java standards, and uses proper logging (SLF4J instead of `System.out.println`).
- [ ] **Basic Testing**: Core business logic (e.g., complex calculations) is covered by unit tests.
- [ ] **Manual Verification**: Endpoints are manually tested (Postman/Swagger) and function as expected.

---

## 3. Specific Module DoD

### 3.1 Backend API Endpoints
- [ ] DTO inputs are properly validated (`@Valid`, `@NotNull`).
- [ ] Global error handling (`@ControllerAdvice`) returns standard JSON error responses.
- [ ] Appropriate HTTP status codes are returned (2xx, 4xx, 5xx).
- [ ] Secured via Firebase JWT and RBAC (`@PreAuthorize`).

### 3.2 Database & Migrations
- [ ] Schema changes are updated via Flyway/Liquibase or proper JPA entity definition.
- [ ] DB indexes applied on frequently searched columns (e.g., Product `name`, Transaction `status`).
- [ ] No N+1 query problems; use `JOIN FETCH` or `EntityGraphs`.

### 3.3 Payment Gateway (Stripe)
- [ ] Test mode transactions (using `tok_visa` or test cards) succeed end-to-end.
- [ ] Webhook signatures are strictly validated.
- [ ] `reservedStock` deductions and edge cases (e.g., user closing browser mid-payment) are handled flawlessly by the recovery scheduler.

### 3.4 Membership & Points System
- [ ] Tier upgrade logic correctly aggregates completed transaction totals within the active `SpendingCycle`.
- [ ] Point multiplier logic evaluates securely on the backend; frontend display of expected points matches backend reality.
- [ ] Concurrency: If a user has two overlapping transactions completing at the same time, points and spending totals are safely aggregated without race conditions (e.g., using optimistic locking / `@Version`).

### 3.5 Schedulers & Cron Jobs
- [ ] Jobs are annotated with appropriate `@Scheduled` intervals.
- [ ] System handles scheduler failures gracefully without crashing the Spring context.
- [ ] Execution details are logged for monitoring purposes.
- [ ] Membership reset jobs (Downgrades) run idempotently and safely handle DB transaction rollbacks if failure occurs midway.
