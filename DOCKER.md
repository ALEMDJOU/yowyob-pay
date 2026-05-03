# Lancer YowYob Pay avec Docker

## Prérequis

- Docker et Docker Compose v2
- Ports libres : **5432**, **9092**, **8090**, **8091**, **2181**

## Stack complète (recommandé)

À la racine du dépôt :

```bash
cd /chemin/vers/yowyob-pay
docker compose up --build
```

- **Swagger Payment** : [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)
- **Swagger User-Payment** : [http://localhost:8091/swagger-ui.html](http://localhost:8091/swagger-ui.html)
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

---

Les endpoints métier principaux restent sous `/api/v1/...` ; le user-payment exige un JWT sur les routes protégées après login (`/api/v1/auth/login`).
