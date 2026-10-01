# 💳 YowYob Pay · Wallet & payment platform

Team project: a **digital wallet and payment platform** for the YowYob ecosystem, made of a reactive payment microservice and a multilingual web back-office.

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3_WebFlux-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat-square&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js_16-000000?style=flat-square&logo=nextdotjs&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)

---

## 🧩 Components

| Folder | Role | Stack |
|---|---|---|
| `payment-service-main/` | Wallets, transactions, recharges and commissions | Spring Boot 3 WebFlux, R2DBC PostgreSQL, Liquibase, Kafka, Redis, Resilience4j, Spring Security |
| `frontend-payment-main/` | Back-office: dashboard, wallets, transactions, payment and recharge operations | Next.js 16, React 19, next-intl, Tailwind CSS 4 |
| `docker/`, `docker-compose.yml` | Local infrastructure | PostgreSQL 16, Kafka, Zookeeper, Redis |

## ✨ Highlights

- **Reactive API** (`/api/v1/wallets`, `/api/v1/transactions`) built with WebFlux and R2DBC
- **Idempotent payments** through an `Idempotency-Key` header
- **Event-driven processing** with Kafka (wallet creation, payment commissions)
- **Optional Redis cache** for wallet balances, with invalidation on every update
- **Two security chains**: internal API key for business routes, JWT for tooling routes
- **Resilience** with circuit breakers (Resilience4j)
- **Multilingual back-office** with authentication, wallet management and transaction history
- **Database migrations** managed by Liquibase

The functional atlas, Mermaid diagrams and the full change history are documented in [changelogs.md](changelogs.md).

## 🚀 Getting started

**Prerequisites:** Docker and Docker Compose v2.

```bash
git clone https://github.com/ALEMDJOU/yowyob-pay.git
cd yowyob-pay
cp .env.example .env   # then adjust the values
docker compose up --build postgres zookeeper kafka redis payment-service
```

- Swagger UI: <http://localhost:8090/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8090/v3/api-docs>

Business routes require the `X-Internal-Api-Key` header. See [DOCKER.md](DOCKER.md) for ports, environment variables, caching and production settings.

> ℹ️ The `user-payment-service` referenced in `docker-compose.yml` is not included in this repository.

**Frontend:**

```bash
cd frontend-payment-main
npm install
npm run dev
```

## 👥 Team

Developed by the YowYob team, including [@NathanaelZuchuon](https://github.com/NathanaelZuchuon) and [@ALEMDJOU](https://github.com/ALEMDJOU).
