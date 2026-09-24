# 🍽️ Auro-Dining Restaurant Management System

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)
![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue.svg)
![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20SES-FF9900.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)

A modern, full-stack Online-to-Offline (O2O) Restaurant Management System. This project provides a complete solution for both restaurant administrators (B-end) and dining customers (C-end), featuring high-concurrency caching, stateless security, and a fully automated cloud-native CI/CD pipeline.

---

## ✨ Core Features

### 👤 Customer End (C-End)
*   **Dynamic Email Authentication**: Secure login via AWS SES (Simple Email Service) dynamic verification codes.
*   **High-Performance Menu Browsing**: Millisecond-level menu and category loading powered by Redis caching.
*   **Smart Shopping Cart**: Real-time cart state management with complex pricing aggregations.
*   **Order Management**: Seamless order placement and historical order tracking.

### 👨‍🍳 Admin End (B-End)
*   **Employee Management**: Role-based access control and staff onboarding.
*   **Product Lifecycle**: Comprehensive management of dishes, flavors (SKUs), and nested combo meals (Setmeals).
*   **Automated Auditing**: All administrative actions are automatically tracked (Who & When) using Spring Data JPA Auditing.
*   **Order Processing**: Real-time order state machine (Pending -> Preparing -> Delivering -> Completed).

---

## 🏗️ System Architecture

```mermaid
flowchart TB
    subgraph Clients["Clients (Frontend)"]
        C_End["📱 C-End (Customer)"]
        B_End["💻 B-End (Admin)"]
    end

    subgraph Cloud["☁️ AWS Cloud Infrastructure"]
        subgraph Docker["🐳 Docker Environment (EC2)"]
            App["🍃 Auro-Dining (Spring Boot 3.2)<br/>- JPA Auditing<br/>- ThreadLocal Auth<br/>- Jackson Config"]
            Redis["🔴 Redis Cache<br/>- Menu/Dish Cache<br/>- Anti-Penetration"]
        end
        PostgreSQL["🐘 PostgreSQL<br/>- Relational Data<br/>- ACID Transactions"]
    end

    SES["📧 AWS SES<br/>(Simple Email Service)"]
    Github["🐙 GitHub Actions<br/>(CI/CD Pipeline)"]

    C_End -- "REST API (Menu, Cart, Orders)" --> App
    B_End -- "REST API (Staff, Category, SKUs)" --> App
    
    App -- "Cache & Evict" --> Redis
    App -- "Read & Write (ORM)" --> PostgreSQL
    App -- "Trigger Auth Emails" --> SES
    
    Github -- "Automated Build & Deploy" -.-> Docker
```

---

## 🛠️ Tech Stack & Architecture

*   **Backend Framework**: Java 17, Spring Boot 3.2, Spring Data JPA
*   **Database & Caching**: PostgreSQL, Redis (Configured with custom Jackson serialization and cache penetration defense)
*   **Security**: Stateless Filter chains + `ThreadLocal` context isolation
*   **Cloud & DevOps**: AWS EC2, AWS SES, Docker, Docker-Compose, GitHub Actions
*   **Performance Metrics**: JMeter load testing demonstrated an API latency drop from 500ms to <10ms and a 15x throughput increase (up to 3000 QPS) after cache optimization.

---

## 🚀 Quick Start (Local Development)

### Prerequisites
*   [Java 17+](https://adoptium.net/)
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/) (for easy Redis setup)
*   Maven 3.8+

### 1. Start External Services (Database & Cache)
If you have Docker installed, you can quickly spin up the required Redis instance:
```bash
docker-compose -f deploy/docker-compose.yml up redis -d
```
*(Ensure PostgreSQL is running locally on port 5432 with a 'restaurant' database)*

### 2. Configure AWS Secrets (Optional)
For email verification to work, configure your AWS SES credentials in `application.yml` or your environment variables. 
*(Note: If AWS is not configured, the app will gracefully fallback to logging the verification codes in the console).*

### 3. Build & Run
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```
The API will be available at `http://localhost:80`.

---

## ☁️ Cloud Deployment (CI/CD)

This project features a **Zero-Downtime CI/CD Pipeline** built with GitHub Actions.
1. Push code to the `main` branch.
2. GitHub Actions automatically compiles the Java 17 artifact.
3. The package is securely transferred to **AWS EC2** via SCP.
4. `docker-compose` rebuilds the container and performs a smooth restart.
5. Automated health checks ensure 99.9% availability. (Includes custom 10-second rollback `.sh` scripts in the `deploy/` folder).

---
*Developed with ❤️ and modern Java engineering practices.*
