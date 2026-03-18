# Definition of Done — Backend

> **Version:** 2.0 | **Date:** 2026-03-18

---

## Feature-Level DoD

Every backend feature must satisfy **all** of the following criteria before it is considered "done":

### Code Quality
- [ ] Code compiles with zero warnings
- [ ] Follows layered architecture: Controller → Service → Repository
- [ ] No entity exposed directly in API response (DTO boundary enforced)
- [ ] MapStruct mapper exists for every DTO ↔ Entity transformation
- [ ] Lombok annotations used consistently (`@Data`, `@Builder`, etc.)
- [ ] No hardcoded secrets or configuration values (use env vars)

### API Design
- [ ] Endpoint follows RESTful conventions (proper HTTP methods + status codes)
- [ ] Request/Response DTOs defined with appropriate validation annotations
- [ ] Error responses use standardized JSON format via `@ControllerAdvice`
- [ ] Pagination implemented for all list endpoints (no unbounded queries)
- [ ] Authentication/Authorization tier correctly assigned (Public / User / Admin)

### Business Logic
- [ ] Service layer contains all business rules (not in Controller or Repository)
- [ ] `@Transactional` applied to all write operations
- [ ] Edge cases handled (null inputs, empty collections, boundary values)
- [ ] Optimistic locking used where concurrent modification is possible
- [ ] Cache invalidation implemented for any cacheable data mutations

### Data Integrity
- [ ] Database schema supports the feature (entities, relationships, indexes)
- [ ] Unique constraints enforced at DB level (not just application level)
- [ ] Snapshot pattern used where historical accuracy is required
- [ ] Enums used for fixed-value domains (MembershipLevel, PaymentStatus, etc.)

### Security
- [ ] Endpoint protected by appropriate Spring Security role
- [ ] No SQL injection vectors (JPA parameterized queries only)
- [ ] Sensitive data excluded from API responses
- [ ] Stripe webhook signature verified before processing

### Testing
- [ ] Feature manually tested via API client (Postman / Insomnia)
- [ ] Happy path verified
- [ ] Error/edge cases verified (invalid input, unauthorized access, conflicts)

### Documentation
- [ ] API endpoint documented in `API_CONTRACT.md`
- [ ] Functional requirement documented in `FUNCTIONAL_REQUIREMENTS.md`
- [ ] Use case updated in `USE_CASES.md` (if new actor flow)
- [ ] ADR written for significant architectural decisions

---

## Deployment-Level DoD

- [ ] Docker image builds successfully via `./gradlew jib`
- [ ] Container starts without errors on Lightsail
- [ ] Environment variables configured in `.env`
- [ ] Health check endpoint responds (Spring Actuator `/health`)
- [ ] Stripe webhook endpoint accessible from Stripe Dashboard

---

## Sprint/Release DoD

- [ ] All planned features meet Feature-Level DoD
- [ ] No critical bugs in production
- [ ] Database migrations applied successfully
- [ ] Redis cache cleared/warmed if schema changed
- [ ] Deployment Guide updated if infrastructure changed
