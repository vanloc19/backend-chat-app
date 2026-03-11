# 🗨️ Backend Architecture — Zalo-like Chat App

> A microservices-based real-time chat backend inspired by Zalo's architecture.
> Services are coordinated through a central `api-gateway`.

---

## 📐 System Overview

```mermaid
flowchart LR
    Client(["🖥️ Mobile / Web Client"])

    subgraph GW ["🔀 API Gateway (Go · :5000)"]
        direction TB
        Router["Reverse Proxy\nPrefix-based routing\nWebSocket upgrade"]
    end

    subgraph SVC ["⚙️ Microservices"]
        direction TB
        Auth["🔐 auth-service\nSpring Boot · :5001\nJWT · MongoDB"]
        Friend["👥 friend-services\nNestJS · :5002\nMongoDB"]
        Msg["💬 messager-services\nGo · WebSocket · :5003\nPostgres"]
        Notif["🔔 notification-services\nNestJS · :5004\nMongoDB"]
    end

    subgraph DB ["🗄️ Databases"]
        direction TB
        MongoAuth[("MongoDB\nauth")]
        MongoFriend[("MongoDB\nfriend")]
        PgMsg[("Postgres\nmessages")]
        MongoNotif[("MongoDB\nnotification")]
    end

    Client -->|"HTTPS / WS"| GW

    GW -->|"/auth/*"| Auth
    GW -->|"/friends/*"| Friend
    GW -->|"/messager/* 🔌 WS"| Msg
    GW -->|"/notifications/*"| Notif

    Auth --> MongoAuth
    Friend --> MongoFriend
    Msg --> PgMsg
    Notif --> MongoNotif
```

> **Notes**
> - Gateway routing is prefix-based: `/auth`, `/friends`, `/messager`, `/notifications`
> - `/messager` supports **WebSocket upgrades** for real-time messaging

---

## 🔑 Auth Flow

> Register / Login → JWT issuance → Authorized calls to downstream services

```mermaid
sequenceDiagram
    autonumber

    participant C  as 🖥️ Client
    participant GW as 🔀 API Gateway
    participant A  as 🔐 Auth Service
    participant M  as 🗄️ MongoDB (auth)
    participant S  as ⚙️ Downstream Service

    rect rgba(56, 139, 253, 0.08)
        note over C,M: ① Register
        C  ->> GW : POST /auth/register · (phone, password, displayName)
        GW ->> A  : Forward /register
        A  ->> M  : Create user (hashed password)
        A  -->> C : 200 OK + JWT 🎫
    end

    rect rgba(63, 185, 80, 0.08)
        note over C,M: ② Login
        C  ->> GW : POST /auth/login · (phone, password)
        GW ->> A  : Forward /login
        A  ->> M  : Verify credentials
        A  -->> C : 200 OK + JWT 🎫
    end

    rect rgba(210, 153, 34, 0.08)
        note over C,S: ③ Authorized Call
        C  ->> GW : Any protected endpoint · Authorization: Bearer <JWT>
        GW ->> S  : Forward request (propagate auth header)
        S  -->> C : Response ✅
    end
```

---

## 📊 Service Matrix

| Service | Stack | Port | Responsibility |
|---|---|:---:|---|
| `api-gateway` | Go · reverse proxy | **5000** | Single entry point — routes by path prefix, handles WS upgrade |
| `auth-service` | Java 17 · Spring Boot · Spring Security · MongoDB | **5001** | Phone-based registration & login, password hashing, JWT issuance |
| `friend-services` | NestJS · Mongoose · MongoDB | **5002** | Friend graph — requests, accepts, blocks, contact search |
| `messager-services` | Go · Gorilla WebSocket · Postgres | **5003** | Real-time chat hub, message persistence, online indicators |
| `notification-services` | NestJS · Mongoose · MongoDB | **5004** | Out-of-band notifications (friend requests, new messages) |

---

## ⚙️ Key Configuration

```
# MongoDB — include database name in URI
MONGO_URI=mongodb://localhost:27017/auth       # auth-service
MONGO_URI=mongodb://localhost:27017/friend     # friend-services
MONGO_URI=mongodb://localhost:27017/notification  # notification-services

# Postgres — messager-services (Supabase: use Session Pooler for IPv4)
DATABASE_URL=postgresql://user:pass@host:5432/messages
```

> **Tip — Supabase + IPv4**: if your network is IPv4-only, use the **Session Pooler** connection string instead of the direct connection to avoid connection issues.