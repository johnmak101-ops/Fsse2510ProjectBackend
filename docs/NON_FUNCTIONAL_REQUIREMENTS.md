# Non-Functional Requirements — Backend

> **Version:** 2.0 | **Date:** 2026-03-18

---

## NFR-01: Performance

| ID | Requirement | Target | Measurement |
|:---|:---|:---|:---|
| NFR-01.1 | API response time (P95) for read endpoints | < 200ms | Application metrics |
| NFR-01.2 | API response time (P95) for write endpoints | < 500ms | Application metrics |
| NFR-01.3 | Product list query with Two-Step Fetch | < 150ms | Query profiling |
| NFR-01.4 | Redis cache hit rate for product reads | > 80% | Redis metrics |
| NFR-01.5 | Database connection pool efficiency | < 50% utilization at normal load | HikariCP metrics |
| NFR-01.6 | Stripe PaymentIntent creation latency | < 2s (dependent on Stripe API) | API monitoring |

---

## NFR-02: Security

| ID | Requirement | Target | Implementation |
|:---|:---|:---|:---|
| NFR-02.1 | All API endpoints enforce authentication/authorization | 100% coverage | Spring Security filter chain |
| NFR-02.2 | JWT validation on every request (stateless) | Zero session state | Firebase Auth + OAuth2 Resource Server |
| NFR-02.3 | Stripe webhook signature verification | All webhook events verified | `Webhook.constructEvent()` |
| NFR-02.4 | No sensitive data in API responses | No passwords, secret keys | DTO mapping layer |
| NFR-02.5 | CORS configured for frontend origin only | Single allowed origin | `APP_FRONTEND_URL` env var |
| NFR-02.6 | SQL injection prevention | Zero vulnerabilities | JPA parameterized queries |
| NFR-02.7 | Admin role assignment via environment config | Principle of least privilege | `ADMIN_EMAILS` env var |

---

## NFR-03: Scalability

| ID | Requirement | Target | Strategy |
|:---|:---|:---|:---|
| NFR-03.1 | Stateless architecture for horizontal scaling | N instances behind LB | JWT auth, no server sessions |
| NFR-03.2 | Database read offloading | 80%+ cached reads | Redis caching layer |
| NFR-03.3 | Pagination for all list endpoints | No unbounded queries | Cursor-based `Slice<T>` |
| NFR-03.4 | Container-ready deployment | Single `docker compose up` | Google Jib + Docker Compose |

---

## NFR-04: Reliability

| ID | Requirement | Target | Implementation |
|:---|:---|:---|:---|
| NFR-04.1 | Payment idempotency | No duplicate charges | Stripe PaymentIntent ID + optimistic locking |
| NFR-04.2 | Dual payment confirmation | Webhook + frontend status | Lazy reconciliation pattern |
| NFR-04.3 | Data consistency on concurrent writes | No race conditions | `@Version` optimistic locking on Transaction |
| NFR-04.4 | Graceful error handling | All exceptions mapped to HTTP status | `@ControllerAdvice` global handler |
| NFR-04.5 | Transaction rollback on failure | Auto-rollback on exception | Spring `@Transactional` |

---

## NFR-05: Maintainability

| ID | Requirement | Target | Implementation |
|:---|:---|:---|:---|
| NFR-05.1 | Clean separation of concerns | Controller → Service → Repository | Layered architecture |
| NFR-05.2 | Zero manual DTO mapping | MapStruct auto-generation | Compile-time mapper generation |
| NFR-05.3 | Consistent code style | Lombok for boilerplate | `@Data`, `@Builder`, `@AllArgsConstructor` |
| NFR-05.4 | Configuration externalization | All secrets in env vars | `.env` file, not hardcoded |
| NFR-05.5 | Schema evolution support | JPA auto-DDL in dev | `spring.jpa.hibernate.ddl-auto` |

---

## NFR-06: Availability

| ID | Requirement | Target | Strategy |
|:---|:---|:---|:---|
| NFR-06.1 | Application uptime | 99%+ | Docker restart policy: `always` |
| NFR-06.2 | JVM tuning for Lightsail (1GB instance) | Stable under 1.5GB memory | `-Xms512m -Xmx1g -XX:MaxMetaspaceSize=160m` |
| NFR-06.3 | Health check endpoint | Spring Actuator `/health` | Container orchestration readiness |

---

## NFR-07: Observability

| ID | Requirement | Target | Implementation |
|:---|:---|:---|:---|
| NFR-07.1 | Structured error responses | Consistent JSON format across all endpoints | `GlobalExceptionHandler` |
| NFR-07.2 | Cache operation logging | Hit/miss/evict events logged | Spring Cache logging |
| NFR-07.3 | Payment event logging | All Stripe events logged | Webhook handler logging |
