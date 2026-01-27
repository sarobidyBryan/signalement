# Système d'Authentification et Dashboard - Backoffice Signaleo

## 🎯 Vue d'ensemble

Ce système implémente un backoffice complet avec authentification et tableau de bord pour la gestion des signalements.

## ✨ Fonctionnalités

### Backend (Spring Boot)
- ✅ **API d'authentification** (`/api/auth`)
  - Login avec email/password
  - Gestion de session avec cookies
  - Récupération de l'utilisateur connecté
  - Logout

- ✅ **API Dashboard** (`/api/dashboard`)
  - Statistiques globales (total signalements, en cours, terminés, etc.)
  - Signalements récents
  - Répartition par statut
  - Vue d'ensemble complète

### Frontend (React + Vite)
- ✅ **Page de Login** (`/backoffice`)
  - Formulaire de connexion moderne
  - Gestion des erreurs
  - Redirection automatique si déjà connecté

- ✅ **Dashboard** (`/backoffice/dashboard`)
  - Affichage des statistiques en temps réel
  - Cartes interactives avec animations
  - Sidebar de navigation
  - Protection par authentification

- ✅ **Composants réutilisables**
  - ProtectedRoute pour sécuriser les routes
  - Service API centralisé
  - Gestion d'état avec localStorage

## 🚀 Démarrage

### Backend
```bash
cd backend/signalement
mvn clean install
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8180`

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Le frontend démarre sur `http://localhost:5173`

## 📋 Utilisation

### 1. Connexion
1. Accédez à `http://localhost:5173/backoffice`
2. Entrez vos identifiants (email et mot de passe)
3. Cliquez sur "Se connecter"

### 2. Dashboard
Après connexion, vous serez redirigé vers le dashboard qui affiche :
- **Total Signalements** : Nombre total de signalements
- **Signalements Assignés** : Signalements avec attribution
- **En Cours** : Signalements avec statut IN_PROGRESS
- **Terminés** : Signalements COMPLETED + VERIFIED
- **En Attente** : Signalements PENDING
- **Entreprises** : Nombre total d'entreprises
- **Utilisateurs Actifs** : Nombre d'utilisateurs

### 3. Navigation
Utilisez la sidebar pour naviguer entre les sections :
- Tableau de bord
- Signalements
- Entreprises
- Utilisateurs
- Configurations
- Déconnexion

## 🔒 Sécurité

- **Sessions côté serveur** : Utilisation de sessions HTTP avec cookies
- **Routes protégées** : Le composant `ProtectedRoute` empêche l'accès non autorisé
- **Redirection automatique** : Les utilisateurs non connectés sont redirigés vers le login
- **Persistance** : Les informations utilisateur sont stockées dans localStorage

## 🎨 Design

Le frontend utilise un design moderne avec :
- **Variables CSS** pour une cohérence visuelle
- **Animations** sur les cartes au survol
- **Responsive design** adapté à tous les écrans
- **États de chargement** et d'erreur
- **Icônes emoji** pour une meilleure UX

## 📡 API Endpoints

### Authentification
- `POST /api/auth/login` - Connexion
- `POST /api/auth/logout` - Déconnexion
- `GET /api/auth/current-user` - Utilisateur connecté

### Dashboard
- `GET /api/dashboard/stats` - Statistiques globales
- `GET /api/dashboard/recent-reports?limit=10` - Signalements récents
- `GET /api/dashboard/reports-by-status` - Répartition par statut
- `GET /api/dashboard/overview` - Vue d'ensemble complète

## 🔧 Configuration

### Variables d'environnement (Frontend)
Créez un fichier `.env` dans le dossier `frontend/` :

```env
VITE_API_URL=http://localhost:8080/api
```

### Application Properties (Backend)
```properties
# Session configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false
```

## 📦 Structure des fichiers

```
frontend/src/
├── services/
│   └── api.js              # Service API centralisé
├── components/
│   ├── ProtectedRoute.jsx  # Protection des routes
│   ├── Sidebar/            # Navigation latérale
│   ├── Card/               # Cartes de statistiques
│   └── ...
└── pages/
    ├── BackofficeLogin.jsx # Page de connexion
    └── Dashboard.jsx       # Tableau de bord

backend/src/main/java/mg/itu/s5/cloud/signalement/
├── controllers/
│   ├── AuthenticationController.java
│   └── DashboardController.java
├── services/
│   ├── AuthenticationService.java
│   └── DashboardService.java
└── repositories/
    └── ReportsStatusRepository.java
```

## 🐛 Débogage

### Problème : "Erreur de connexion au serveur"
- Vérifiez que le backend est démarré sur le port 8080
- Vérifiez la configuration CORS dans le backend
- Vérifiez l'URL dans le fichier `.env`

### Problème : "Session expirée"
- La session expire après 30 minutes d'inactivité
- Reconnectez-vous via la page de login

### Problème : "Données non chargées"
- Vérifiez que la base de données contient des données
- Consultez les logs du backend pour les erreurs
- Vérifiez la console du navigateur

## 🚧 Développement futur

- [ ] Gestion des rôles et permissions
- [ ] Filtres et recherche avancée
- [ ] Graphiques et visualisations
- [ ] Export de données
- [ ] Notifications en temps réel
- [ ] Mode sombre

## 📝 Licence

Ce projet fait partie du système Signaleo Cloud.
