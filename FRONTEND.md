# Frontend — Usage & Docker build (Vite + React)

## Objectif
Donner une configuration Docker propre et reproductible pour le frontend Vite + React, tout en offrant une **option contrôlée** (build-arg) pour activer un fallback temporaire `--legacy-peer-deps` si nécessaire. L'approche recommandée reste : corriger les dépendances et committer `package-lock.json`.

## Prérequis
- Node.js (>=18) et npm installés localement
- `package-lock.json` doit être généré et commité dans le repo

## Scénarios courants et commandes

### 1) Réinstaller / mettre à jour React localement
- Mettre à jour React (ex. vers 18 si nécessaire pour `react-leaflet@4.x`) :
  ```bash
  cd frontend
  npm install react@18.2.0 react-dom@18.2.0
  # ou pour réinstaller complètement
  rm -rf node_modules
  npm install
  git add package.json package-lock.json
  git commit -m "Update / reinstall frontend deps and lockfile"
  ```

### 2) Vérifier les conflits de peer-deps avant de builder
```bash
cd frontend
npm ls react
npm outdated
npx npm-check-updates -u   # facultatif pour mettre à jour package.json
npm install
# vérifier que package-lock.json est propre
git add package-lock.json && git commit -m "Update lockfile"
```

### 3) Build local (prod) et test
```bash
cd frontend
npm run build
npm run preview   # optionnel pour servir localement
```

## Docker — Build clair et reproductible
Le `Dockerfile` est conçu pour :
- utiliser `npm ci --omit=dev` quand un `package-lock.json` valide est présent (meilleure pratique),
- proposer un **fallback contrôlé** via la build-arg `USE_LEGACY_PEER_DEPS=true` si vous **connaissez** qu'une dépendance bloquante empêche `npm ci`.

- Build standard (préféré):
  ```bash
  docker compose build frontend
  # ou depuis le dossier frontend
  docker build -t projet-frontend:local .
  ```

- Build **avec fallback** (temporaire, à n'utiliser que pour débogage) :
  ```bash
  docker build --build-arg USE_LEGACY_PEER_DEPS=true -t projet-frontend:legacy .
  ```
  ou avec docker compose :
  ```bash
  docker compose build --build-arg USE_LEGACY_PEER_DEPS=true frontend
  ```

- Lancer :
  ```bash
  docker compose up --build
  ```

## Bonnes pratiques
- **Ne pas** laisser l'image finale dépendre d'installations non reproductibles. Si `--legacy-peer-deps` est nécessaire, corrigez ensuite les versions dans `package.json` (ou mettez à jour le paquet tiers) et générez un `package-lock.json` correct.
- Garder `package-lock.json` dans le contrôle de version pour des builds reproductibles.
- Si vous changez les dépendances, testez d'abord localement (`npm ci` puis `npm run build`).

## Debug rapide si build échoue
1. Exécutez localement `npm ci` dans `frontend/` (vérifiez qu'il réussit).
2. Si `npm ci` échoue localement, corrigez les dépendances, sinon vous pouvez temporairement builder Docker avec `USE_LEGACY_PEER_DEPS=true` pour obtenir une image et investiguer.

---

Voulez-vous que je génère et commette le `package-lock.json` maintenant (cela installera dépendances ici et mettra à jour le repo) ?
