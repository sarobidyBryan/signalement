# Backend — Usage & Docker build (Spring Boot)

## Prérequis
- Java 21 compatible (JDK 21)
- Maven (optionnel localement)

## Build local (packaging)
1. Construire le JAR (mode batch, sans tests) :
   ```bash
   cd backend/signalement
   mvn -B -DskipTests package
   # le jar se trouve dans target/
   ```
2. Lancer localement (sans Docker) :
   ```bash
   java -jar target/*.jar
   # ou si vous avez copié/nommé app.jar : java -jar app.jar
   ```

## Base de données & migrations
- Le projet est configuré avec `spring.jpa.hibernate.ddl-auto=none` (AUCUNE création automatique de tables).
- Utilisez un outil de migration (Fortement recommandé) : Flyway ou Liquibase.
  - Exemple ajout Flyway (pom.xml):
    ```xml
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    ```
  - Mettez vos scripts SQL dans `src/main/resources/db/migration` et Flyway appliquera les migrations au démarrage.

## Docker (build & run)
- Build de l'image backend (depuis la racine du repo) :
  ```bash
  docker compose build backend
  ```
  ou (depuis `backend/signalement`)
  ```bash
  docker build -t projet-backend:local .
  ```
- Lancer via docker compose :
  ```bash
  docker compose up --build
  ```

## En cas de problème de build Maven dans Docker
- Le Dockerfile utilise maintenant une commande Maven non interactive : `mvn -B -DskipTests package` afin d'éviter que la construction bloque sur des téléchargements ou invites.
- Si vous avez des problèmes réseau au niveau du build, essayez localement `mvn -B -DskipTests package` pour valider.

## Tests & debug
- Exécutez les tests locaux :
  ```bash
  cd backend/signalement
  mvn test
  ```
- Pour inspecter les logs du container backend :
  ```bash
  docker compose logs -f backend
  ```

---

Besoin d'aide pour ajouter Flyway et créer la première migration SQL ? Je peux générer un exemple de script de migration et la configuration Flyway.
