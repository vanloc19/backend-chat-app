# Backend Architecture (Zalo-like Chat App)

This repository contains a microservices-based backend (Zalo-like chat app). Services are coordinated through an `api-gateway`.

## System overview

```mermaid
flowchart LR
  Client[Mobile/Web Client]
  GW[api-gateway\nGo reverse proxy :5000]

  Auth[auth-service\nSpring Boot + MongoDB :5001]
  Friend[friend-services\nNestJS + MongoDB :5002]
  Msg[messager-services\nGo + WebSocket + Postgres :5003]
  Notif[notification-services\nNestJS + MongoDB :5004]

  MongoAuth[(MongoDB: auth)]
  MongoFriend[(MongoDB: friend)]
  MongoNotif[(MongoDB: notification)]
  PgMsg[(Postgres: messages)]

  Client -->|HTTP / WS| GW

  GW -->|/auth/*| Auth
  GW -->|/friends/*| Friend
  GW -->|/messager/*| Msg
  GW -->|/notifications/*| Notif

  Auth --> MongoAuth
  Friend --> MongoFriend
  Notif --> MongoNotif
  Msg --> PgMsg
```

Notes:
- Gateway routing is prefix-based (`/auth`, `/friends`, `/messager`, `/notifications`).
- `/messager` supports WebSocket upgrades.

## Auth flow (Register / Login → JWT → Authorized calls)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant GW as API Gateway
  participant A as Auth Service
  participant M as MongoDB (auth)
  participant S as Downstream Service<br/>(friends/messager/notifications)

  rect rgb(245,245,245)
    C->>GW: POST /auth/register (phone, password, displayName)
    GW->>A: Forward /register
    A->>M: Create user (hashed password)
    A-->>C: 200 + JWT
  end

  rect rgb(245,245,245)
    C->>GW: POST /auth/login (phone, password)
    GW->>A: Forward /login
    A->>M: Verify credentials
    A-->>C: 200 + JWT
  end

  rect rgb(235,250,235)
    C->>GW: Request to protected endpoint<br/>Authorization: Bearer <JWT>
    GW->>S: Forward request (and optionally propagate auth header)
    S-->>C: Response
  end
```

## Service matrix

| Service | Tech | Port | Responsibility |
|---|---|---:|---|
| `api-gateway` | Go (reverse proxy) | 5000 | Single entry point, routes requests by path prefix |
| `auth-service` | Java 17, Spring Boot, MongoDB, Spring Security, JWT | 5001 | User auth, JWT issuance |
| `friend-services` | NestJS, Mongoose (MongoDB) | 5002 | Friend graph / relationships |
| `messager-services` | Go, Gorilla WebSocket, Postgres | 5003 | Real-time chat (WS) + persistence |
| `notification-services` | NestJS, Mongoose (MongoDB) | 5004 | Notifications |

## Key configuration (high level)

- **Mongo services**: ensure the MongoDB URI includes a database name (e.g. `/auth`, `/friend`, `/notification`) or explicitly set the database name via env vars.
- **Postgres**: `messager-services` uses a Postgres DSN (Supabase may require Session Pooler for IPv4-only networks).

