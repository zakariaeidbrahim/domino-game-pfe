## DominoGame (PFE2 + PFE3) — Spring Boot + MySQL + React

Application web de domino 1v1 en ligne.

### Stack
- **Backend**: Java 8, Spring Boot (REST + WebSocket STOMP), Spring Security + JWT, JPA/Hibernate
- **DB**: MySQL 8 (Docker)
- **Frontend**: React (Vite) + TypeScript

---

## Démarrage rapide (local)

### Prérequis (important)
- **JDK** installé (pas seulement un JRE). Vérifie que `javac -version` marche.
  - Recommandé: Temurin JDK 8 (compatible Spring Boot 2.7) ou JDK 17 (si tu passes plus tard à Spring Boot 3).
- Docker Desktop (pour MySQL)

### 1) Lancer MySQL
Depuis `dominos-pfe/`:

```bash
docker compose up -d
```

### 2) Lancer le backend
Depuis `dominos-pfe/backend/`:

```bash
./mvnw.cmd spring-boot:run
```

Backend: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Si ton `java -version` pointe vers un **JRE 8** (comme chez toi), lance le backend avec un **JDK**:

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-23
mvnw.cmd spring-boot:run
```

### 3) Lancer le frontend
Depuis `dominos-pfe/frontend/`:

```bash
npm install
npm run dev
```

Frontend: `http://localhost:5173`

---

## Fonctionnalités
- Auth (register/login) via JWT
- Lobby (créer une partie / rejoindre par code)
- Partie domino 1v1:
  - distribution 7 dominos
  - pose validée par règles (matching extrémités)
  - pioche si aucun coup jouable (si stock non vide)
  - pass si stock vide et aucun coup jouable
  - fin de partie (main vide ou blocage) + calcul score
- Temps réel via WebSocket STOMP (événements de partie)

---

## Variables / configuration
- MySQL: voir `docker-compose.yml`
- Backend: `backend/src/main/resources/application.yml`
- Frontend: `frontend/src/config.ts`


## 👤 Contact & Developer Info

**Zakariae IDBRAHIM**  
*Fourth-year Cybersecurity Engineering Student at ENSIASD*

*   **Portfolio:** [idbrahimzakariae.me](https://idbrahimzakariae.me)
*   **LinkedIn:** [www.linkedin.com/in/zakariae-idbrahim-198bba2b5](#) 
*   **GitHub:** [@zakariaeidbrahim](https://github.com/zakariaeidbrahim)
*   **Email:** idbrahimzakariae1@gmail.com

---
*Developed as part of my Full-stack development projects.*
