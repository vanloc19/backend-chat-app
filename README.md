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

## Auth flow (Register / Login -> JWT -> Authorized calls)

```mermaid
flowchart TD

  subgraph OTP["1 · POST /auth/send-otp"]
    direction LR
    c1([Client]) -->|phoneNumber| a1[auth-service]
    a1 -->|"store OTP=190603, TTL=120s, rate-limit=60s"| r1[(Redis)]
  end

  subgraph REG["2 · POST /auth/register  —  phone + otp + password + displayName"]
    direction LR
    c2([Client]) --> a2[auth-service]
    a2 -->|"1. verify OTP"| r2[(Redis)]
    a2 -->|"2. create profile"| p2[("users / profiles")]
    a2 -.->|"3. register device (optional)"| d2[device-service]
    a2 -->|"4. store refresh token"| t2[("auth / refresh_tokens")]
    a2 --> j2(["accessToken + refreshToken"])
  end

  subgraph VOTP["3 · POST /auth/verify-otp  —  phone + otp"]
    direction LR
    c3([Client]) --> a3[auth-service]
    a3 -->|"1. verify OTP"| r3[(Redis)]
    a3 -->|"2. find or create profile"| p3[("users / profiles")]
    a3 -.->|"3. register device (optional)"| d3[device-service]
    a3 -->|"4. store refresh token"| t3[("auth / refresh_tokens")]
    a3 --> j3(["accessToken + refreshToken"])
  end

  subgraph LOGIN["4 · POST /auth/login  —  phone + password"]
    direction LR
    c4([Client]) --> a4[auth-service]
    a4 -->|"1. load profile + verify passwordHash"| p4[("users / profiles")]
    a4 -.->|"2. register device (optional)"| d4[device-service]
    a4 -->|"3. store refresh token"| t4[("auth / refresh_tokens")]
    a4 --> j4(["accessToken + refreshToken"])
  end

  subgraph REFRESH["5 · POST /auth/refresh  —  refreshToken"]
    direction LR
    c5([Client]) --> a5[auth-service]
    a5 -->|"1. validate + rotate token"| t5[("auth / refresh_tokens")]
    a5 -->|"2. load user"| p5[("users / profiles")]
    a5 --> j5(["new accessToken + refreshToken"])
  end

  subgraph PROTECTED["6 · Authorized API call  —  Authorization: Bearer accessToken"]
    direction LR
    c6([Client]) -->|"JWT validated at gateway"| gw[API Gateway]
    gw --> svc["/users  /devices  /friends  /messenger  /notifications"]
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

