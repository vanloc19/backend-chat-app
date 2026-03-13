# Backend Architecture (Zalo-like Chat App)

This repository contains a microservices-based backend (Zalo-like chat app). Services are coordinated through an `api-gateway`.

## System overview

```mermaid
flowchart LR
  Client[Mobile/Web Client]
  GW[api-gateway\nGo reverse proxy :5000]

  Auth[auth-service\nSpring Boot + MongoDB :5001]
  Friend[friend-services\nNestJS + MongoDB :5002]
  Msg[messenger-services\nGo + WebSocket + Postgres :5003]
  Notif[notification-services\nNestJS + MongoDB :5004]
  Users[users-services\nSpring Boot + MongoDB :5005]
  Device[device-service\nSpring Boot :5006]
  Redis[(Redis :6379)]
  Rabbit[(RabbitMQ :5672 / :15672)]

  MongoAuth[(MongoDB: auth)]
  MongoFriend[(MongoDB: friend)]
  MongoNotif[(MongoDB: notification)]
  PgMsg[(Postgres: messages)]

  Client -->|HTTP / WS| GW

  GW -->|/auth/*| Auth
  GW -->|/friends/*| Friend
  GW -->|/messenger/*| Msg
  GW -->|/notifications/*| Notif
  GW -->|/users/*| Users
  GW -->|/devices/*| Device

  Auth --> MongoAuth
  Auth --> Redis
  Friend --> MongoFriend
  Notif --> MongoNotif
  Msg --> PgMsg
  Users --> MongoAuth
  Friend -.events.-> Rabbit
  Msg -.events.-> Rabbit
  Notif -.consume.-> Rabbit
```

Notes:
- Gateway routing is prefix-based (`/auth`, `/friends`, `/messenger`, `/notifications`, `/users`, `/devices`).
- `/messenger` supports WebSocket upgrades.

## Event-driven architecture

```mermaid
flowchart LR
  Friend[friend-services]
  Msg[messenger-services]
  Notif[notification-services]
  Rabbit[(RabbitMQ)]

  Friend -->|publish: friend.requested| Rabbit
  Friend -->|publish: friend.accepted| Rabbit
  Msg -->|publish: message.created| Rabbit
  Notif -->|consume events| Rabbit
```

Notes:
- Event broker uses RabbitMQ (`amqp://rabbitmq:5672`) in docker-compose.
- Notification service can subscribe to domain events and fan-out notifications.

Quick test endpoints:
- Publish friend event: `POST http://localhost:5002/friends/request`
- Inspect consumed events: `GET http://localhost:5004/notifications/events`

## Auth flow (Register / Login -> JWT -> Authorized calls)

```mermaid
flowchart LR
  Client[Client]
  GW[API Gateway\nGo reverse proxy :5000]

  Auth[auth-service\nSpring Boot :5001]
  DevSvc[device-service]

  Redis[(Redis\nOTP store)]
  Profiles[(MongoDB\nusers / profiles)]
  Tokens[(MongoDB\nauth / refresh_tokens)]

  Services[Protected Services\n/users  /friends\n/messenger  /notifications]

  Client -->|POST /auth/send-otp| GW
  Client -->|POST /auth/register| GW
  Client -->|POST /auth/verify-otp| GW
  Client -->|POST /auth/login| GW
  Client -->|POST /auth/refresh| GW
  Client -->|Bearer accessToken| GW

  GW -->|public auth routes| Auth
  GW -->|JWT valid - proxy| Services

  Auth -->|store & verify OTP| Redis
  Auth -->|read & write profile| Profiles
  Auth -.->|register device - optional| DevSvc
  Auth -->|issue & rotate token| Tokens
  Auth -->|accessToken + refreshToken| Client
```

## Service matrix

| Service | Tech | Port | Responsibility |
|---|---|---:|---|
| `api-gateway` | Go (reverse proxy) | 5000 | Single entry point, routes requests by path prefix |
| `auth-services` | Java 17, Spring Boot, MongoDB, Spring Security, JWT | 5001 | User auth, OTP flow, JWT issuance |
| `friend-services` | NestJS, Mongoose (MongoDB) | 5002 | Friend graph / relationships |
| `messenger-services` | Go, Gorilla WebSocket, Postgres | 5003 | Real-time chat (WS) + persistence |
| `notification-services` | NestJS, Mongoose (MongoDB) | 5004 | Notifications |
| `users-services` | Java 17, Spring Boot, MongoDB | 5005 | User profile read/update APIs |
| `device-service` | Java 17, Spring Boot | 5006 | Device registration metadata |

## Infra matrix

| Infra | Port | Role |
|---|---:|---|
| `redis` | 6379 | OTP cache + short-lived session/cache data |
| `rabbitmq` | 5672 / 15672 | Event bus for async publish/consume |

## Key configuration (high level)

- **Mongo services**: ensure the MongoDB URI includes a database name (e.g. `/auth`, `/friend`, `/notification`) or explicitly set the database name via env vars.
- **Postgres**: `messenger-services` uses a Postgres DSN (Supabase may require Session Pooler for IPv4-only networks).
- **Redis**: `auth-services` reads `REDIS_URL` and `REDIS_SSL_ENABLED`.
- **RabbitMQ**: services can use `MESSAGE_BROKER_URL=amqp://guest:guest@rabbitmq:5672`.

## Infra folder flow

- Infra definitions are centralized in `infra/docker/docker-compose.infra.yml`.
- Root `docker-compose.yml` extends infra services (`redis`, `rabbitmq`) from that file.
- Run stack from backend root as usual:

```bash
docker compose up -d --build
```

## Backend folder structure (clean style)

```text
backend/
├ services/
│  ├ api-gateway/
│  ├ auth-services/
│  ├ users-services/
│  ├ friend-services/
│  ├ messenger-services/
│  ├ notification-services/
│  └ device-service/
├ shared/
│  ├ logger/
│  ├ middleware/
│  ├ utils/
│  ├ libs/
│  └ types/
├ infra/
│  ├ docker/
│  │  ├ docker-compose.infra.yml
│  │  ├ Dockerfile.auth
│  │  └ Dockerfile.user
│  ├ kafka/
│  │  └ docker-compose.kafka.yml
│  ├ redis/
│  │  └ redis.conf
│  └ nginx/
│     └ nginx.conf
├ docker-compose.yml
└ README.md
```

