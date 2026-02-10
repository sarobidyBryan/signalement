# Signaleo — Plateforme de Signalement Routier

> Application web de gestion des signalements de dégradation routière à Antananarivo, avec backoffice d'administration, carte interactive et synchronisation bidirectionnelle Firebase / PostgreSQL.




## Architecture

```
┌──────────────┐      ┌───────────────┐      ┌─────────────────────┐
│   Frontend   │──────│   Backend     │──────│   PostgreSQL 15     │
│  React/Vite  │      │  Spring Boot  │      │   + PostGIS         │
│  Nginx proxy │──────│  Java 21      │──────│                     │
└──────┬───────┘      └───────┬───────┘      └─────────────────────┘
       │                      │
       │                      │              ┌─────────────────────┐
       │                      └──────────────│   Firebase          │
       │                        Firestore    │                     │
       │                        sync         └─────────────────────┘
       │
       │              ┌───────────────┐
       └──────────────│  TileServer   │
         /styles/*    │  MapTiler GL  │
         /data/*      │  (mbtiles)    │
         /fonts/*     └───────────────┘
```

Tous les services sont orchestrés via **Docker Compose**. Le frontend Nginx agit comme reverse proxy :
- `/api/*` → backend Spring Boot
- `/styles/*`, `/data/*`, `/fonts/*` → TileServer GL

---

## Stack technique

| Couche | Technologie | Version |
|---|---|---|
| **Frontend** | React + Vite + Leaflet | React 19, Vite 7, Leaflet 1.9 |
| **Backend** | Spring Boot + Hibernate Spatial | Spring Boot 3.2.1, Java 21 |
| **Base de données** | PostgreSQL + PostGIS | PostgreSQL 15, PostGIS 3 |
| **Cloud / BaaS** | Firebase Admin SDK | firebase-admin 9.2.0 |
| **Cartographie** | TileServer GL (MapTiler) | latest |
| **Conteneurisation** | Docker + Docker Compose | Compose v3.9 |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) | springdoc 2.2.0 |
| **Serveur web** | Nginx (Alpine) | latest |
| **Build tools** | Maven 3.9, npm/Node 20 | — |

---

## Prérequis

- **Docker Desktop** installé et démarré (Windows / macOS / Linux)
- **Git** pour cloner le repository
- Fichier de credentials Firebase (`.json`) — fourni dans le projet

> **Aucune installation de Java, Node.js ou PostgreSQL n'est nécessaire** : tout est conteneurisé.

---

## Structure du projet

```
projet-cloud/
├── .env                          # Variables d'environnement (DB + Firebase)
├── docker-compose.yml            # Orchestration des 4 services
├── start.ps1                     # Script de lancement Windows (PowerShell)
├── README.md                     # Ce fichier
│
├── backend/signalement/          # API REST Spring Boot
│   ├── Dockerfile                # Multi-stage build Maven → JRE 21
│   ├── pom.xml                   # Dépendances Maven
│   ├── src/main/java/            # Code source Java
│   │   └── mg/itu/s5/cloud/signalement/
│   │       ├── config/           # FirebaseConfig, OpenApiConfig
│   │       ├── controllers/      # 13 REST Controllers
│   │       ├── dto/              # Data Transfer Objects
│   │       ├── entities/         # Entités JPA (15 tables)
│   │       ├── repositories/     # Spring Data JPA Repositories
│   │       ├── services/         # Logique métier + sync Firebase
│   │       └── utils/            # Utilitaires (ApiResponse, etc.)
│   └── src/main/resources/
│       └── application.properties
│
├── frontend/                     # SPA React
│   ├── Dockerfile                # Multi-stage build Node → Nginx
│   ├── nginx.conf                # Reverse proxy (API + TileServer)
│   ├── package.json              # Dépendances npm
│   └── src/
│       ├── components/           # Composants réutilisables
│       ├── pages/                # Pages de l'application
│       ├── services/             # Clients HTTP (fetch vers /api)
│       └── styles/               # CSS global + variables de thème
│
├── db/                           # Base de données PostgreSQL
│   ├── Dockerfile                # PostgreSQL 15 + PostGIS 3
│   └── initdb/                   # Scripts d'initialisation (ordre alphabétique)
│       ├── 01-init-postgis.sql   # Extensions PostGIS
│       ├── 02-init-database.sql  # Schéma complet + données initiales
│       └── 03-insert-sample-report.sql
│
└── tileserver/data/              # Tuiles cartographiques
    ├── antananarivo.mbtiles      # Données OSM Antananarivo
    ├── config.json               # Configuration TileServer GL
    ├── fonts/                    # Polices pour le rendu cartographique
    └── styles/                   # Styles de carte (osm-bright)
```

---

## Configuration

### Fichier `.env` (racine du projet)

Le fichier `.env` est lu automatiquement par Docker Compose. Il contient :

```env
# ─── Base de données ───
POSTGRES_DB=signalements
POSTGRES_USER=appuser
POSTGRES_PASSWORD=apppassword

# ─── Firebase ───
FIREBASE_PROJECT_ID=signalement-cloud-s5
FIREBASE_CREDENTIALS={"type":"service_account","project_id":"...","private_key":"...","client_email":"..."}
```

> **Important** : `FIREBASE_CREDENTIALS` contient le JSON complet du service account Firebase sur **une seule ligne**. Ne pas ajouter de retours à la ligne dans la valeur.

### Fichier Firebase credentials

Le fichier `backend/signalement/signalement-cloud-s5-firebase-adminsdk-fbsvc-56c957d58f.json` est monté en volume dans le conteneur backend à `/app/config/firebase-credentials.json`. Le backend le lit au démarrage via `FirebaseConfig.java`.

---

## Lancement avec Docker


### Démarrage rapide (Linux / macOS)

```bash
docker compose up --build
```

### Commandes utiles

```bash
# Démarrer en arrière-plan
docker compose up -d --build

# Rebuild un seul service (ex: backend après modification)
docker compose up -d --build backend

# Voir les logs en temps réel
docker compose logs -f
docker compose logs -f backend     # logs d'un seul service

# Arrêter tous les services
docker compose down

# Arrêter et supprimer les volumes (reset complet de la BDD)
docker compose down -v

# Status des services
docker compose ps
```

### Ordre de démarrage

Docker Compose gère les dépendances automatiquement :

1. **postgres** — démarre en premier. Healthcheck : `pg_isready`
2. **backend** — attend que postgres soit `healthy`. Healthcheck : test TCP port 8080
3. **frontend** — attend que backend soit `healthy`
4. **tileserver** — indépendant, démarre en parallèle

> Le premier build peut prendre **10–15 minutes** (téléchargement des dépendances Maven + npm).

---

## Accès aux services

| Service | URL | Description |
|---|---|---|
| **Frontend** | [http://localhost:3000](http://localhost:3000) | Application web (SPA React) |
| **Backend API** | [http://localhost:8180/api](http://localhost:8180/api) | API REST Spring Boot |
| **Swagger UI** | [http://localhost:8180/swagger-ui.html](http://localhost:8180/swagger-ui.html) | Documentation interactive de l'API |
| **API Docs (JSON)** | [http://localhost:8180/api-docs](http://localhost:8180/api-docs) | Spécification OpenAPI 3.0 |
| **TileServer** | [http://localhost:8081](http://localhost:8081) | Serveur de tuiles cartographiques |
| **PostgreSQL** | `localhost:5532` | Accès direct à la base (via pgAdmin, DBeaver, etc.) |

### Connexion à la base de données

```
Host:     localhost
Port:     5532
Database: signalements
User:     appuser
Password: apppassword
```

### Documentation complète

Accédez à **Swagger UI** pour une documentation interactive avec possibilité de tester chaque endpoint :

```
http://localhost:8180/swagger-ui.html
```

---



## Licence
Projet universitaire — Signaleo — ITU  — Semestre 5 Cloud 
3331 — 3251 — 3335 — 3293