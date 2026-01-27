# Projet Cloud - Spring Boot + React + PostGIS

## Prérequis
- Docker Desktop installé et démarré
- PowerShell (Windows) ou Bash (Linux/Mac)

## Lancement rapide

### Windows
```powershell
.\start.ps1
```

### Linux/Mac
```bash
docker compose up --build
```

## Architecture

- **Backend**: Spring Boot 3.2.1 + Java 21 + Hibernate Spatial
- **Frontend**: React + Vite + Leaflet
- **Database**: PostgreSQL 15 + PostGIS
- **Tileserver**: MapTiler GL pour les cartes d'Antananarivo

## Ports
- Frontend: http://localhost:3000
- Backend: http://localhost:8180
- Backend (container internal): http://backend:8080 (used by frontend container proxy)
- Database: localhost:5432
- Tileserver: http://localhost:8081

## Configuration requise

Créez un fichier `.env` à la racine:
```env
POSTGRES_DB=signalement_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

## Troubleshooting

### Build Maven échoue avec erreur DNS
1. Redémarrez Docker Desktop
2. Ou relancez le build: `docker compose build --no-cache backend`
3. Le script `start.ps1` configure automatiquement les DNS

### Les services ne démarrent pas
Vérifiez les logs: `docker compose logs -f [service]`

## Développement

### Backend uniquement
```bash
cd backend/signalement
./mvnw spring-boot:run
```

### Frontend uniquement
```bash
cd frontend
npm install
npm run dev
```
