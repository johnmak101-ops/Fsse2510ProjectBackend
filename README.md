# FSSE2510 Project E-Commerce Backend 🛍️

![Java Version](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Stripe](https://img.shields.io/badge/Stripe-v14-indigo)
![Firebase](https://img.shields.io/badge/Firebase-Admin-yellow)

## 📖 Introduction
This repository contains the backend REST API for the FSSE2510 E-Commerce course project. It is built to support the core functionalities of an online store, handling tasks such as managing products, users, shopping carts, and orders. The API integrates with Firebase Authentication for user login and Stripe for processing checkout payments.

**🚀 Live Demo (Frontend):** [https://johnmak.store](https://johnmak.store)

### 🌟 Project Features
*   **Core E-Commerce Flow**: Basic implementation of cart, checkout, and inventory updates.
*   **Membership System**: A straightforward tiered system to explore points calculation and membership cycles.
*   **Promotions & Discounts**: Practice implementing rule-based logic for applying coupons and promotions to carts.
*   **Admin CMS Support**: API endpoints designed to help manage the homepage display and category navigation.

## 📚 Official Documentation

The system is fully documented in the `docs` directory. Click the links below to view the documentation directly on GitHub:

### Architecture & Requirements
1. [Business Requirements Document (BRD)](./docs/01-BRD.md)
2. [Functional Specification Document (FSD)](./docs/02-FSD.md)
3. [System Architecture](./docs/08-Architecture.md)
4. [Database Schema & ER Diagram](./docs/07-Database-Schema.md)

### Interactions & Interfaces
5. [Use Cases](./docs/03-UseCases.md)
6. [API Specification](./docs/04-API-Spec.md)

### Quality Assurance (QA)
7. [Test Cases](./docs/05-TestCases.md)
8. [Definition of Done (DoD)](./docs/06-DoD.md)

## 🛠️ Tech Stack & Architecture

### Core Frameworks
*   **Language**: Java 21 (Records, Virtual Threads)
*   **Framework**: Spring Boot 3.5.8
*   **Build Tool**: Maven / Gradle

### Data & Persistence
*   **Relational Database**: MySQL 8.0 (Hibernate / Spring Data JPA)
*   **Caching & Session**: Redis (for caching heavy queries and temporary state)
*   **DTO Mapping**: MapStruct

### Security & Integrations
*   **Authentication/Authorization**: Firebase Admin SDK (JWT Bearer Token verification)
*   **Payment Gateway**: Stripe API (Processing and Webhooks)
*   **API Documentation**: Swagger / Springdoc OpenAPI 3

---
*Created for FSSE2510 E-Commerce Project Setup Phase.*
