# Lancer YowYob Pay avec Docker

## Prérequis

- Docker et Docker Compose v2
- Ports libres : **5432**, **6379** (Redis), **9092**, **8090**, **8091**, **2181**

## Stack complète (recommandé)

À la racine du dépôt :

```bash
cd /chemin/vers/yowyob-pay
docker compose up --build
```

- **Swagger Payment** : [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)
- **Swagger User-Payment** : [http://localhost:8091/swagger-ui.html](http://localhost:8091/swagger-ui.html)

### Sécurité (phase 1)

- **Payment-service** : les routes `/api/v1/wallets/**` et `/api/v1/transactions/**` exigent l’en-tête HTTP **`X-Internal-Api-Key`** avec la valeur de `PAYMENT_INTERNAL_API_KEY` (voir `.env.example` à la racine). Dans Swagger UI, configurez le schéma de sécurité **internalApiKey** (bouton « Authorize ») avant d’appeler ces endpoints.
- **Variables** : `JWT_SECRET`, `PAYMENT_INTERNAL_API_KEY`, `EXPOSE_OPENAPI`, `CORS_ALLOWED_ORIGINS`, `MAX_TRANSACTION_AMOUNT`, `MAX_RECHARGE_AMOUNT` - détaillées dans [changelogs.md](changelogs.md) et [.env.example](.env.example).
- **Profil `prod`** : OpenAPI / Swagger désactivés par défaut ; Actuator limité à `health` ; contraintes JWT côté user-payment (expiration ≤ 24 h).

### Latence et cache (phase performance)

- **Redis** : cache solde optionnel côté `payment-service` (profil `docker` : `CACHE_REDIS_ENABLED=true` par défaut dans le compose). Invalidation après mise à jour de portefeuille / transaction. TTL maximal de lecture « sale » ≈ `CACHE_WALLET_BALANCE_TTL` (ex. `PT30S`).
- **Pool R2DBC** : ajuster `R2DBC_POOL_MAX_SIZE` et `R2DBC_POOL_MAX_ACQUIRE_TIME` pour la saturation vs latence (voir `.env.example`).
- **Pagination wallets** : `GET /api/v1/wallets?page=0&size=20` avec en-tête `X-Internal-Api-Key` - paramètres `page` et `size` **obligatoires** (`size` ≤ 50).

- **OpenAPI JSON Payment** : [http://localhost:8090/v3/api-docs](http://localhost:8090/v3/api-docs)
- **OpenAPI JSON User-Payment** : [http://localhost:8091/v3/api-docs](http://localhost:8091/v3/api-docs)

Variables optionnelles : copier `.env.example` vers `.env` et ajuster (sinon les défauts du `docker-compose.yml` s’appliquent).

Arrêt :

```bash
docker compose down
```

## Un seul microservice (réseau partagé)

1. Démarrer uniquement l’infra :

```bash
docker compose up -d postgres zookeeper kafka
```

1. Vérifier que le réseau `yowyob-net` existe (créé par l’étape 1).

2. Dans `payment-service-main` ou `user-payment-service-main` :

```bash
cd payment-service-main
docker compose up --build
```

Le fichier `.env` du projet doit utiliser `DB_HOST` et `KAFKA_HOST` alignés avec Docker (`postgres`, `kafka` sur le même réseau).

## Exécution locale Maven (sans Docker pour les JAR)

1. Démarrer l’infra avec le compose racine (sans les services applicatifs) :

```bash
docker compose up -d postgres zookeeper kafka
```

1. Copier les `.env` des sous-projets et lancer avec le profil `docker` :

```bash
cd payment-service-main
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

Même principe pour `user-payment-service-main` (port **8091**).

## Frontend Next.js (BFF)

1. Démarrer la stack Docker (payment **8090**, user-payment **8091** exposé sur l’hôte).
2. Copier `frontend-payment-main/.env.local.example` vers `frontend-payment-main/.env.local` (aligner `JWT_SECRET` et `PAYMENT_INTERNAL_API_KEY` avec la racine).
3. Lancer l’app :

```bash
cd frontend-payment-main
npm install
npm run dev
```

- UI : [http://localhost:3000](http://localhost:3000) (locale par défaut `/fr`)
- Le navigateur appelle uniquement `/api/bff/*` ; le BFF ajoute `X-Internal-Api-Key` et le cookie httpOnly JWT vers les microservices.

---

Les endpoints métier principaux restent sous `/api/v1/...` ; le user-payment exige un JWT sur les routes protégées après login (`/api/v1/auth/login`).
