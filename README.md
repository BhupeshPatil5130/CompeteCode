<div align="center">

# ⚡ CompetCode

### The Anti-Gravity Competitive Coding Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev)
[![PostgreSQL](https://img.shields.io/badge/Neon-PostgreSQL-00E5CC?style=for-the-badge&logo=postgresql&logoColor=white)](https://neon.tech)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

A full-stack competitive coding platform featuring a real-time multiplayer BattleGround, AI-powered problem analysis, Monaco IDE, DSA roadmaps, ELO ratings, and animated story-mode explanations.

</div>

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🧑‍💻 **Online IDE** | Monaco Editor with multi-language support, stdin console, and real-time code execution via Judge0 |
| ⚔️ **BattleGround** | Live 1v1 or group coding duels with room codes, live leaderboard, and ELO updates |
| 🤖 **AI Test Checker** | Google Gemini generates hidden test cases and evaluates your submission with feedback |
| 🗺️ **DSA Roadmaps** | Structured learning paths (Arrays → DP → Graphs) with progress tracking |
| 📖 **Story Mode** | Step-by-step animated visual walkthroughs of problems powered by Gemini |
| ⚡ **Speed Mode** | Timed challenges to sharpen your coding speed |
| 🏆 **Leaderboard** | Global ELO rankings updated after every battle |
| 📊 **Profile & History** | Submission history, battle history, and personal stats |
| 📋 **Sheets** | Curated problem sheets (like Striver's A2Z) |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React 19, Vite, TailwindCSS, Monaco Editor, Framer Motion, Three.js, React Router v7 |
| **Backend** | Spring Boot 4.0, Spring Security (JWT), Spring Data JPA, WebSocket (STOMP) |
| **Database** | Neon (serverless PostgreSQL) |
| **AI** | Google Gemini 2.0 Flash API |
| **Code Execution** | Judge0 CE API |
| **Realtime** | STOMP over WebSocket (SockJS) |
| **Deployment** | Docker + Nginx |

---

## 📁 Project Structure

```
CompetCode/
├── Backend/                          # Spring Boot REST API
│   ├── src/main/java/com/CompetCode/
│   │   ├── config/                   # Security, WebSocket, CORS, DataSeeder
│   │   ├── controller/               # REST endpoint controllers
│   │   ├── dto/                      # Request / Response DTOs
│   │   ├── entity/                   # JPA entities
│   │   ├── exception/                # Global exception handling
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── security/                 # JWT filter, UserDetails
│   │   └── service/                  # Business logic, Gemini, Judge0
│   ├── src/main/resources/
│   │   └── application.properties
│   └── Dockerfile
│
├── Frontend/                         # React SPA
│   ├── src/
│   │   ├── pages/                    # Home, ProblemIDE, BattleGround, BattleRoom,
│   │   │                             #   Leaderboard, Roadmaps, StoryMode, SpeedMode,
│   │   │                             #   Sheets, Profile, Login, Register, ...
│   │   ├── components/               # Navbar, Footer, CodeEditor, AnimatedExplanation,
│   │   │                             #   Scene3D, TeamBattleLeaderboard, ...
│   │   ├── api/                      # Axios API client
│   │   ├── context/                  # Auth context (JWT)
│   │   └── data/                     # Static seed data
│   ├── nginx.conf                    # Production Nginx config
│   └── Dockerfile
│
├── docker-compose.yml                # Orchestrates backend + frontend
├── .env.example                      # Environment variable template
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (for containerised deployment)
- **OR** locally: Java 21+, Node.js 20+, Maven

---

### 🐳 Option A — Docker (Recommended)

**1. Clone the repo**
```bash
git clone https://github.com/your-username/CompetCode.git
cd CompetCode
```

**2. Set up environment variables**
```bash
cp .env.example .env
```
Then open `.env` and fill in your API keys:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>/<db>?sslmode=require&channel_binding=require
SPRING_DATASOURCE_USERNAME=your_neon_username
SPRING_DATASOURCE_PASSWORD=your_neon_password
JWT_SECRET=your-secure-256-bit-secret
GEMINI_API_KEY=your_gemini_key
JUDGE0_KEY=your_judge0_key
```

**3. Build and launch**
```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| Backend API | http://localhost:8080 |
| Database | Neon Cloud ☁️ |

> First build takes ~3–5 min (Maven + npm install). Subsequent builds are fast due to layer caching.

---

### 💻 Option B — Local Development

**1. Backend**
```bash
cd Backend
# Set environment variables first (see .env.example)
./mvnw spring-boot:run
```
Backend runs at `http://localhost:8080`

**2. Frontend**
```bash
cd Frontend
npm install
npm run dev
```
Frontend runs at `http://localhost:5173` with Vite proxy to backend.

---

## 🔑 Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | ✅ | JDBC URL for Neon PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | ✅ | Neon DB username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | Neon DB password |
| `JWT_SECRET` | ✅ | HS256 signing secret (min 256 bits) |
| `GEMINI_API_KEY` | ✅ | [Google AI Studio](https://aistudio.google.com/apikey) API key |
| `JUDGE0_KEY` | ⚠️ Optional | [RapidAPI Judge0 CE](https://rapidapi.com/judge0-official/api/judge0-ce) key |
| `JUDGE0_URL` | ⚠️ Optional | Judge0 base URL (defaults to `https://judge0-ce.p.rapidapi.com`) |

---

## 🌐 API Reference

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new account |
| `POST` | `/api/auth/login` | Login → returns JWT + refresh token |
| `POST` | `/api/auth/refresh` | Refresh access token |

### Problems
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/problems` | List all problems |
| `GET` | `/api/problems/{id}` | Get problem details |
| `GET` | `/api/problems/{id}/explain` | AI animated explanation (Gemini) |
| `POST` | `/api/submissions` | Submit code for AI evaluation |
| `GET` | `/api/submissions/history` | User's submission history |

### BattleGround
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/battles` | Create a battle room |
| `POST` | `/api/battles/join/{roomCode}` | Join a battle |
| `POST` | `/api/battles/{id}/start` | Start the battle (host only) |
| `POST` | `/api/battles/{id}/end` | End battle and update ELO |
| `GET` | `/api/battles/history` | User's battle history |

### Other
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/roadmaps` | All DSA roadmaps |
| `GET` | `/api/leaderboard` | Global ELO leaderboard |
| `GET` | `/api/users/me` | Current user profile |
| `WS` | `/ws/battle` | STOMP WebSocket for live battles |

---

## ⚔️ BattleGround Flow

```
Host creates battle → receives room code
       ↓
Participants join via room code
       ↓
Host starts battle → countdown timer begins
       ↓
All participants code in Monaco IDE simultaneously
       ↓
First to pass all test cases wins (or best score at timeout)
       ↓
ELO ratings update based on final rankings
```

---

## 🌱 Seed Data

On first startup the backend automatically seeds:
- **3 Problems**: Two Sum, Reverse String, Maximum Subarray
- **2 DSA Roadmaps**: Arrays & Hashing, Dynamic Programming

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">
Built with ❤️ by the CompetCode team
</div>
