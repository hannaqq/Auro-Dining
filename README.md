# 馃嵔锔?Auro-Dining Restaurant Management System

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)
![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue.svg)
![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20SES-FF9900.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)

A modern cloud native Restaurant Ordering and Management System. This project provides a complete solution for both restaurant administrators and dining customers, featuring high-concurrency caching, stateless security, and a fully automated cloud-native CI/CD pipeline.

---

## 馃洜锔?Tech Stack & Architecture

*   **Backend Framework**: Java 17, Spring Boot 3.2, Spring Data JPA
*   **Database & Caching**: PostgreSQL, Redis (Configured with custom Jackson serialization and cache penetration defense)
*   **Security**: Stateless Filter chains + `ThreadLocal` context isolation
*   **Cloud & DevOps**: AWS EC2, AWS SES, Docker, Docker-Compose, GitHub Actions
*   **Performance Metrics**: JMeter load testing demonstrated an API latency drop from 500ms to <10ms and a 15x throughput increase (up to 3000 QPS) after cache optimization.

---

## 鉁?Core Features

### 馃懁 Customer Portal
*   **Dynamic Email Authentication**: Secure login via AWS SES dynamic verification codes.
*   **High-Performance Menu Browsing**: Millisecond-level menu and category loading powered by Redis caching.
*   **Smart Shopping Cart**: Real-time cart state management with complex pricing aggregations.
*   **Order Management**: Seamless order placement and historical order tracking.

### 馃懆鈥嶐煃?Admin Dashboard
*   **Employee Management**: Role-based access control and staff onboarding.
*   **Product Lifecycle**: Comprehensive management of dishes, flavors (SKUs), and nested combo meals (Setmeals).
*   **Automated Auditing**: All administrative actions are automatically tracked (Who & When) using Spring Data JPA Auditing.
*   **Order Processing**: Real-time order state machine (Pending -> Preparing -> Delivering -> Completed).

---

## 馃彈锔?System Architecture

```mermaid
flowchart TB
    subgraph Clients["Clients (Frontend)"]
        CustomerPortal["📱 Customer Portal"]
        AdminDashboard["💻 Admin Dashboard"]
    end

    subgraph Cloud["☁️ AWS Cloud Infrastructure"]
        subgraph Docker["🐳 Docker Environment (EC2)"]
            AuthFilter["🛡️ Security Filter Chain<br/>(Stateless OTP)"]
            App["🍃 Auro-Dining Core<br/>(Spring Boot 3.2)"]
            Redis["🔴 Redis Cache<br/>(Cache-Aside & Fallback)"]
            
            AuthFilter ==>|"Validated Request"| App
        end
        
        SES["📧 AWS SES<br/>(Email Service)"]
        PostgreSQL["🐘 PostgreSQL<br/>(ACID Transactions)"]
    end

    Github["🐙 GitHub Actions<br/>(CI/CD Pipeline)"]

    CustomerPortal -- "REST API" --> AuthFilter
    AdminDashboard -- "REST API" --> AuthFilter
    
    App -.->|"1. Cache & Evict"| Redis
    App ===>|"2. Read & Write"| PostgreSQL
    App --->|"3. Trigger Emails"| SES
    
    Github -. "Automated Deploy" .-> Docker
```

### 馃攷 Under the Hood: High-Availability Cache Workflow
To handle peak dining hours and ensure system resilience, the caching layer implements the **Cache-Aside Pattern** with a custom **Graceful Degradation (Fallback)** mechanism.

```mermaid
flowchart TD
    %% Read Flow (Customer)
    Client([Customer Queries Menu]) --> Controller[Dish & Combo Controllers<br/>@Cacheable]
    Controller --> CacheAOP{Spring Cache AOP}
    
    CacheAOP -- "Try Cache" --> RedisStatus{Redis Healthy?}
    
    %% Normal Flow
    RedisStatus -- "Yes (Alive)" --> CacheHit{Cache Hit?}
    CacheHit -- "Hit" --> JacksonDes[Jackson Deserialization]
    JacksonDes --> Return([Return JSON Fast])
    
    CacheHit -- "Miss" --> DBQuery[Query PostgreSQL]
    DBQuery --> JacksonSer[Jackson Serialization + 1hr TTL]
    JacksonSer --> SaveRedis[Save to Redis]
    SaveRedis --> Return
    
    %% Fallback Flow (Graceful Degradation)
    RedisStatus -- "No (Timeout/Down)" --> ErrorHandler[Custom CacheErrorHandler]
    ErrorHandler -- "Mute Exception (Pretend Miss)" -.-> DBQuery
    
    %% Cache Consistency (Admin)
    Admin([Admin Updates Dish/Combo]) --> AdminController[Dish & Combo Controllers<br/>@CacheEvict]
    AdminController --> UpdateDB[Update PostgreSQL]
    UpdateDB -- "Cache Aside" --> DeleteCache[Evict Redis Cache]
    
    %% Styling
    classDef fallback fill:#ffebee,stroke:#c62828,stroke-width:2px,color:#c62828;
    class ErrorHandler fallback;
```

---



### 馃攼 Under the Hood: Stateless Auth & Memory Safety
To support distributed deployments and strict memory management, the authentication flow uses a **Zero-Frontend-Modification JWT strategy** combined with isolated `ThreadLocal` context management.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Browser)
    participant Filter as LoginCheckFilter
    participant Ctrl as UserController
    participant Redis as Redis Cache
    participant SES as AWS SES
    participant TL as AuthContext (ThreadLocal)
    participant Service as Business Services

    %% Phase 1: OTP Request
    rect rgb(245, 247, 250)
        Note over Client, SES: Phase 1: OTP Generation & Delivery
        Client->>Ctrl: POST /user/sendMsg (Email)
        Ctrl->>SES: Send 6-digit OTP
        Ctrl->>Redis: SET email:OTP (TTL: 5 mins)
        Ctrl-->>Client: 200 OK (OTP Sent)
    end

    %% Phase 2: Login & JWT
    rect rgb(240, 248, 255)
        Note over Client, Redis: Phase 2: Stateless Login (Zero-Frontend-Mod)
        Client->>Ctrl: POST /user/login (Email, OTP)
        Ctrl->>Redis: GET email:OTP & Validate
        Ctrl->>Redis: DEL email:OTP (Prevent Replay Attack)
        Note over Ctrl: Sign JWT (HS512)
        Ctrl-->>Client: 200 OK (Set-Cookie: Auth-Token=JWT)
    end

    %% Phase 3: Authenticated Request
    rect rgb(253, 245, 230)
        Note over Client, Service: Phase 3: Auth Context & Memory Leak Prevention
        Client->>Filter: Request Business API (Cookie: Auth-Token)
        Filter->>Filter: Parse & Verify JWT
        Filter->>TL: setCurrentId(userId)
        
        Filter->>Service: doFilter() (Proceed to Controller/Service)
        Service->>TL: getCurrentId()
        TL-->>Service: Return userId
        Service-->>Filter: Return Business Response
        
        Note right of Filter: finally block execution
        Filter->>TL: removeCurrentId() (Prevent Memory Leak)
        Filter-->>Client: 200 OK Response
    end
```

---

## 馃殌 Quick Start (Local Development)

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

## 鈽侊笍 Cloud Deployment (CI/CD)

This project features a fully automated **CI/CD Pipeline** leveraging GitHub Actions and AWS infrastructure:
1. Push code to the `main` branch.
2. GitHub Actions automatically compiles the Java 17 artifact and builds the Docker image.
3. The image is deployed to an **AWS EC2** instance running as a purely stateless compute node.
4. Data persistence is offloaded to **Amazon RDS (PostgreSQL)**, completely decoupling storage from compute for enterprise-grade durability.
5. Spring Boot Actuator performs automated HTTP health checks (`/actuator/health`) to verify container readiness during deployment.

---
*Developed with 鉂わ笍 and modern Java engineering practices.*

