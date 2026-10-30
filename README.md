# 🎫 Mini Helpdesk — Sistem Tiket Internal BJB

Sistem manajemen tiket helpdesk internal untuk Bank BJB. Dibangun sebagai fullstack monorepo dengan Spring Boot di backend dan Svelte di frontend.

---

## 📦 Struktur Monorepo

```
mini-helpdesk/
├── mini-helpdesk-service/   → Backend (Spring Boot)
├── mini-helpdesk-web/       → Frontend (Svelte)
└── .amazonq/                → Dokumentasi & panduan internal
```

---

## 🏗️ Tech Stack

### Backend
| Teknologi | Versi |
|-----------|-------|
| Java | 21 (LTS) |
| Spring Boot | 3.3.0 |
| Spring Security | JWT Stateless |
| Spring Data JPA | Hibernate ORM |
| PostgreSQL | Database utama |
| Lombok | Boilerplate reduction |

### Frontend
| Teknologi | Versi |
|-----------|-------|
| Svelte | 4 |
| Vite | 5 |
| Tailwind CSS | 3 |
| Axios | 1.6 |

---

## 🚀 Cara Menjalankan

### Prerequisites
- Java 21
- Node.js 18+
- PostgreSQL (running di port 5432)
- Database `bjb_helpdesk_db` sudah dibuat

### Backend
```bash
cd mini-helpdesk-service
mvn spring-boot:run
```
Server berjalan di `http://localhost:8080/api`

### Frontend
```bash
cd mini-helpdesk-web
npm install
npm run dev
```
App berjalan di `http://localhost:5173`

---

## ⚙️ Environment Variables

### Backend (`mini-helpdesk-service/src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bjb_helpdesk_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
app:
  jwt:
    secret: ${JWT_SECRET:your-secret-key}
    access-token-expiration: 900000      # 15 menit
    refresh-token-expiration: 604800000  # 7 hari
```

### Frontend (`mini-helpdesk-web/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 👥 Role & Akses

| Role | Akses |
|------|-------|
| `STAFF` | Buat tiket, lihat tiket milik sendiri |
| `DIVISION_HEAD` | Lihat semua tiket, update status, assign tiket |
| `ADMIN` | Full akses termasuk hapus tiket |

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Akses |
|--------|----------|-------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/refresh` | Public |
| POST | `/api/auth/logout` | Authenticated |

### Tickets
| Method | Endpoint | Akses |
|--------|----------|-------|
| POST | `/api/tickets` | Authenticated |
| GET | `/api/tickets` | Authenticated |
| GET | `/api/tickets/{id}` | Authenticated |
| PUT | `/api/tickets/{id}` | Owner / ADMIN / DIVISION_HEAD |
| DELETE | `/api/tickets/{id}` | Owner / ADMIN |

---

## ✅ Progress

### Backend
- [x] Setup project Spring Boot 3.3 + Java 21
- [x] Konfigurasi PostgreSQL + JPA
- [x] Model: `User`, `RefreshToken`
- [x] JWT stateless — access token (15 menit) + refresh token (7 hari)
- [x] Spring Security — filter chain, CORS, stateless session
- [x] Auth flow — register, login, refresh, logout
- [x] Global exception handler
- [x] Fix circular dependency `JwtAuthFilter` ↔ `SecurityConfig`
- [x] Model: `Ticket` — category, priority, status, assignee
- [x] Repository: `TicketRepository` dengan pagination
- [x] DTO: `CreateTicketRequest`, `UpdateTicketRequest`, `TicketResponse`
- [x] Service: `TicketService` — role-based business logic
- [x] Controller: `TicketController` — CRUD dengan pagination

### Frontend
- [x] Setup project Svelte 4 + Vite + Tailwind CSS
- [x] Struktur folder dasar (`src/`, `index.html`, `main.js`, `App.svelte`)

---

## 🗺️ Roadmap

### Phase 1 — Backend Completion
- [ ] Tambah filter tiket by status, category, priority di `GET /tickets`
- [ ] Endpoint statistik tiket — total per status, per kategori (untuk dashboard)
- [ ] Validasi & error handling lebih granular (custom exception per kasus)
- [ ] Unit test — `AuthService`, `TicketService`
- [ ] Integration test — endpoint auth & ticket

### Phase 2 — Frontend Core
- [ ] Setup Svelte stores — `auth.js`, `ticket.js`
- [ ] Setup Axios instance + interceptor (inject token, handle 401)
- [ ] Halaman Login — form, validasi, connect ke `POST /auth/login`
- [ ] Halaman Register
- [ ] Route guard — redirect ke `/login` kalau belum auth
- [ ] Halaman Dashboard — statistik tiket
- [ ] Halaman Ticket List — tabel dengan pagination
- [ ] Halaman Create Ticket — form buat tiket baru
- [ ] Halaman Ticket Detail — lihat detail + update status (ADMIN/DIVISION_HEAD)

### Phase 3 — Enhancement
- [ ] Notifikasi real-time saat tiket di-assign (WebSocket / SSE)
- [ ] Upload attachment pada tiket
- [ ] Komentar/diskusi per tiket
- [ ] Export laporan tiket ke PDF/Excel
- [ ] Halaman manajemen user (ADMIN only)

### Phase 4 — Production Ready
- [ ] Dockerize BE + FE
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Environment staging vs production
- [ ] Rate limiting pada endpoint auth
- [ ] Logging terpusat (Logback + file appender)

---

## 📁 Dokumentasi Internal

Tersedia di folder `.amazonq/`:
- `panduan-modul-ticket.md` — panduan implementasi modul ticket step by step
- `rules/project-context.md` — konteks teknis project
- `rules/mentor.md` — protokol sesi mentoring

---

## 📝 Konvensi Kode

### Backend
| Layer | Konvensi | Contoh |
|-------|----------|--------|
| Controller | `{Domain}Controller` | `TicketController` |
| Service | `{Domain}Service` | `TicketService` |
| Repository | `{Domain}Repository` | `TicketRepository` |
| Entity | PascalCase | `Ticket`, `User` |
| DTO Request | `{Action}{Domain}Request` | `CreateTicketRequest` |
| DTO Response | `{Domain}Response` | `TicketResponse` |

### Frontend
| Entitas | Konvensi | Contoh |
|---------|----------|--------|
| Page/Component | PascalCase | `LoginPage.svelte` |
| Store | camelCase | `auth.js` |
| API module | camelCase + `Api` | `ticketApi.js` |
