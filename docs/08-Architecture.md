# System Architecture & Technical Design

## FSSE2510 E-Commerce Platform

| Item               | Detail                  |
|--------------------|-------------------------|
| **Document Version** | 1.0                   |
| **Project Name**     | FSSE2510 E-Commerce   |

---

## 1. Overview
This document describes the high-level architecture, technology stack, and structural design patterns used in the FSSE2510 E-Commerce Backend Platform.

## 2. Technology Stack

*   **Language**: Java 17+
*   **Framework**: Spring Boot 3.x
*   **Database**: Relational Database (e.g., MySQL / PostgreSQL)
*   **ORM**: Spring Data JPA / Hibernate
*   **Security**: Spring Security + Firebase Admin SDK (JWT Validation)
*   **Payment Integration**: Stripe API (Java SDK)
*   **Documentation**: Swagger / OpenAPI (Springdoc-openapi)
*   **Build Tool**: Gradle / Maven

## 3. High-Level Architecture

The system follows a standard **Monolithic N-Tier Architecture** suitable for API-driven platforms.

### Spring Boot Backend Layer Architecture
```mermaid
graph TD
    Client([Frontend App])
    Controller[Controllers (REST API)]
    Service[Services (Business Logic)]
    Repo[Repositories (Data Access)]
    DB[(MySQL)]
    Redis[(Redis Cache)]
    Firebase[Firebase JWT Auth]
    Stripe[Stripe API]

    Client -->|HTTP/REST| Controller
    Controller -->|Delegates to| Service
    Service -.->|Validates Token| Firebase
    Service -.->|Payment intent| Stripe
    Service -->|Uses Entity| Repo
    Repo -->|JPA/Hibernate| DB
    Service -.->|Cache products/cart| Redis
```

### 3.1 Layers
1. **Presentation Layer (Controllers)**
    *   Responsible for receiving REST API requests (`@RestController`).
    *   Handles DTO validation (`@Valid`).
    *   Returns structured JSON responses (including proper error handling via `@ControllerAdvice`).
2. **Business Logic Layer (Services)**
    *   Contains the core business rules (`@Service`).
    *   Handles transaction management (`@Transactional`).
    *   Integrates with external third-party services (Firebase, Stripe).
3. **Data Access Layer (Repositories)**
    *   Interface extensions of `CrudRepository` or `JpaRepository` (`@Repository`).
    *   Responsible for database communication.
4. **Data Entities (Models)**
    *   JPA Entities (`@Entity`) mapping to database tables.

### 3.2 Key Application Flows

*   **Authentication Flow**:
    1. Frontend logs in via Firebase (Google/Email).
    2. Frontend sends Firebase JWT in the `Authorization: Bearer <token>` header to the Backend.
    3. Backend Spring Security intercepts the request.
    4. Backend validtes the JWT using Firebase Admin SDK.
    5. Backend extracts `uid` and asserts roles based on DB records or custom claims.
*   **Payment Flow**:
    1. User initiates checkout.
    2. Backend calculates total, reserves stock, creates a `PREPARE` transaction, and requests a `PaymentIntent` from Stripe.
    3. Frontend confirms payment with Stripe using the returned `client_secret`.
    4. Stripe sends a webhook to the Backend confirming success or failure.
    5. Backend updates transaction status, deducts reserved stock, clears cart, and awards points.

## 4. Security Patterns

*   **Stateless Authentication**: The backend does NOT use JSESSIONID or server-side sessions. Every request is authenticated independently via JWT.
*   **CORS**: Configured globally to allow frontend domain access.
*   **Environment Variables**: Secrets like database passwords, Stripe API keys, and Firebase JSON configurations are injected via Environment Variables or `.env` / `application.yml` and are **never** committed to version control.

---
*End of Document*
