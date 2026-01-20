# Structure Firestore - Collection Reports

## Vue d'ensemble

La collection `reports` dans Firestore contient les signalements avec leurs assignations et progressions.
La structure a été optimisée pour faciliter les calculs et éviter la duplication des données.

## Structure du document

```json
{
  "postgres_id": 1,
  "id": 1,
  "reportDate": "2024-01-15T10:30:00Z",
  "area": 150.00,
  "longitude": 47.5162,
  "latitude": -18.8792,
  "description": "Route endommagée nécessitant réparation urgente",
  "firebaseId": "abc123xyz",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-20T14:45:00Z",
  
  "user": {
    "postgres_id": 2,
    "id": 2,
    "name": "Alice",
    "email": "alice@example.com",
    "password": "alicepass",
    "firebaseUid": "firebase_uid_alice",
    "role": {
      "id": 1,
      "roleCode": "USER",
      "label": "Utilisateur"
    },
    "userStatusType": {
      "id": 1,
      "statusCode": "ACTIVE",
      "label": "Actif"
    },
    "createdAt": "2024-01-10T08:00:00Z",
    "updatedAt": "2024-01-10T08:00:00Z"
  },
  
  "status": {
    "id": 3,
    "statusCode": "IN_PROGRESS",
    "label": "Travaux en cours"
  },
  
  "assignation": {
    "postgres_id": 5,
    "id": 5,
    "budget": 5000000.00,
    "startDate": "2024-01-20",
    "deadline": "2024-03-20",
    "firebaseId": "assignation_firebase_id",
    "createdAt": "2024-01-20T09:00:00Z",
    "updatedAt": "2024-01-20T09:00:00Z",
    "company": {
      "postgres_id": 1,
      "id": 1,
      "name": "COLAS Madagascar",
      "email": "contact@colas.mg",
      "firebaseId": "company_firebase_id",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  },
  
  "progressions": {
    "reportsAssignationId": 5,
    "reportsAssignationFirebaseId": "assignation_firebase_id",
    "totalTreatedArea": 120.50,
    "totalPercentage": 80.33,
    "remainingArea": 29.50,
    "isCompleted": false,
    "items": [
      {
        "postgres_id": 10,
        "id": 10,
        "treatedArea": 50.00,
        "percentage": 33.33,
        "comment": "Première phase de réparation terminée",
        "registrationDate": "2024-01-25T15:00:00Z",
        "firebaseId": "progress1_firebase_id",
        "createdAt": "2024-01-25T15:00:00Z",
        "updatedAt": "2024-01-25T15:00:00Z"
      },
      {
        "postgres_id": 11,
        "id": 11,
        "treatedArea": 70.50,
        "percentage": 47.00,
        "comment": "Deuxième phase en cours",
        "registrationDate": "2024-02-01T16:30:00Z",
        "firebaseId": "progress2_firebase_id",
        "createdAt": "2024-02-01T16:30:00Z",
        "updatedAt": "2024-02-01T16:30:00Z"
      }
    ]
  },
  
  "synced_at": "2024-02-05T10:00:00Z"
}
```

## Champs principaux

### Report (niveau racine)
- `postgres_id`, `id` : Identifiants du signalement
- `reportDate` : Date du signalement
- `area` : Surface totale à traiter (en m²)
- `longitude`, `latitude` : Coordonnées GPS
- `description` : Description du problème
- `firebaseId` : ID du document dans Firestore
- `createdAt`, `updatedAt` : Dates de création/modification

### User (utilisateur ayant créé le signalement)
- Contient toutes les informations de l'utilisateur
- Inclut le `role` et le `userStatusType` imbriqués

### Status (statut actuel du signalement)
- `statusCode` : Code du statut (SUBMITTED, ASSIGNED, IN_PROGRESS, etc.)
- `label` : Libellé en français

### Assignation (entreprise assignée aux travaux)
- `postgres_id`, `id` : Identifiants de l'assignation
- `budget` : Budget alloué
- `startDate`, `deadline` : Dates de début et de fin prévue
- `company` : **Informations complètes de l'entreprise**
  - Inclut name, email, firebaseId, etc.

### Progressions (progressions globales des travaux)

**Important** : Ce champ remplace l'ancien champ "assignations"

#### Méta-informations
- `reportsAssignationId` : ID PostgreSQL de l'assignation liée
- `reportsAssignationFirebaseId` : ID Firebase de l'assignation liée

#### Totaux calculés
- `totalTreatedArea` : Surface totale traitée (somme de toutes les progressions)
- `totalPercentage` : Pourcentage global d'avancement
- `remainingArea` : Surface restante à traiter
- `isCompleted` : Booléen indiquant si les travaux sont terminés (≥100%)

#### Items (liste des progressions individuelles)
Chaque progression contient :
- `postgres_id`, `id` : Identifiants de la progression
- `treatedArea` : Surface traitée lors de cette progression
- `percentage` : **Pourcentage calculé** pour cette progression (treatedArea / area totale * 100)
- `comment` : Commentaire de l'entreprise
- `registrationDate` : Date de cette progression
- `firebaseId` : ID Firebase de cette progression
- `createdAt`, `updatedAt` : Dates de création/modification

## Services de calcul

### ProgressionCalculationService

Service dédié aux calculs liés aux progressions. Méthodes disponibles :

1. **`calculateProgressionPercentage(treatedArea, totalArea)`**
   - Calcule le pourcentage pour une surface donnée
   - Retourne un BigDecimal arrondi à 2 décimales

2. **`calculateProgressionTotals(progressions, totalArea)`**
   - Calcule totalTreatedArea et totalPercentage
   - Retourne un Map avec les totaux

3. **`calculateCumulativePercentage(progressions, index, totalArea)`**
   - Calcule le pourcentage cumulé jusqu'à une progression donnée
   - Utile pour voir l'évolution chronologique

4. **`isWorkCompleted(totalTreatedArea, totalArea)`**
   - Vérifie si les travaux sont terminés (≥100%)

5. **`calculateRemainingArea(totalTreatedArea, totalArea)`**
   - Calcule la surface restante à traiter

## Avantages de cette structure

1. ✅ **Calculs centralisés** : Tous les calculs sont dans ProgressionCalculationService
2. ✅ **Pas de duplication** : Les infos de company sont dans assignation, pas répétées dans progressions
3. ✅ **Extensible** : Facile d'ajouter de nouveaux calculs dans le service
4. ✅ **Totaux pré-calculés** : totalTreatedArea et totalPercentage directement disponibles
5. ✅ **Pourcentage par progression** : Chaque progression a son pourcentage calculé
6. ✅ **Référence claire** : reportsAssignationId et reportsAssignationFirebaseId pour lier aux données

## Synchronisation

### Postgres → Firebase
```
POST /api/sync/postgres-to-firebase
```

**Synchronise toutes les données depuis PostgreSQL vers Firebase** :
- ✅ Crée les utilisateurs dans Firebase Authentication (si nécessaire)
- ✅ Synchronise les configurations dans Firestore
- ✅ Synchronise les companies dans Firestore
- ✅ Synchronise les users dans Firestore
- ✅ Synchronise les reports avec assignations et progressions dans Firestore
- ✅ Calcule automatiquement les totaux et pourcentages

### Firebase → Postgres
```
POST /api/sync/firebase-to-postgres
```

**Synchronise uniquement les données de base depuis Firebase vers PostgreSQL** :
- ✅ Met à jour les users depuis Firestore
- ✅ Crée/met à jour les reports depuis Firestore
- ❌ **Ne synchronise PAS** les assignations (créées uniquement côté PostgreSQL)
- ❌ **Ne synchronise PAS** les progressions (créées uniquement côté PostgreSQL)

> **Note importante** : Les assignations et progressions sont créées dans PostgreSQL et synchronisées vers Firebase, jamais l'inverse. Firebase est une source de lecture pour ces données.

### Bidirectionnel
```
POST /api/sync/bidirectional
```

Effectue les deux synchronisations dans l'ordre :
1. Postgres → Firebase (tout)
2. Firebase → Postgres (users et reports uniquement)

### Retour de synchronisation

**Postgres → Firebase** :
```json
{
  "success": true,
  "data": {
    "sync_results": {
      "configurations": { "synced": 2, "total_modified": 2 },
      "companies": { "synced": 7, "total_modified": 7 },
      "users": { 
        "synced": 3, 
        "total_modified": 3,
        "authCreated": 1,
        "authExisting": 2
      },
      "reports": { "synced": 5, "total_modified": 5 }
    }
  }
}
```

**Firebase → Postgres** :
```json
{
  "success": true,
  "data": {
    "sync_results": {
      "users": { 
        "total_firestore_docs": 3,
        "synced": 2,
        "created": 0,
        "updated": 2
      },
      "reports": { 
        "total_firestore_docs": 5,
        "synced": 3,
        "created": 1,
        "updated": 2
      }
    }
  }
}
```

## Migration depuis l'ancienne structure

L'ancien champ `assignations` (array d'objets avec progressions imbriquées) a été remplacé par :
- `assignation` : Un seul objet avec les infos de l'assignation et de la company
- `progressions` : Objet structuré avec totaux + items

Cette nouvelle structure évite :
- ❌ La duplication des infos de company dans chaque progression
- ❌ La répétition des calculs côté client
- ❌ La structure profondément imbriquée difficile à maintenir

## Flux de données

```
┌─────────────┐         Sync          ┌──────────────┐
│             │  ──────────────────►  │              │
│  PostgreSQL │                       │   Firebase   │
│             │  Users + Reports      │  (Firestore) │
│             │  ◄──────────────────  │              │
└─────────────┘    Mise à jour        └──────────────┘
                   uniquement

Assignations & Progressions:
  PostgreSQL ──────────► Firebase (lecture seule)
```

**Règle importante** : 
- PostgreSQL est la **source de vérité** pour les assignations et progressions
- Firebase reçoit ces données pour la consultation mobile
- Les modifications d'assignations/progressions se font **uniquement** dans PostgreSQL
