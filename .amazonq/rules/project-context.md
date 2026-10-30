# 📌 Project Context: Mini Helpdesk

---

## 🏛️ Domain & Tujuan

**Mini Helpdesk — Sistem Tiket Internal BJB**

Sistem fullstack untuk manajemen tiket helpdesk internal, autentikasi user, dan pengelolaan laporan/permintaan.

**Fungsi Utama:**
- Autentikasi user (login, register, refresh token)
- Manajemen tiket helpdesk
- Role-based access control

---

## 🏗️ Arsitektur Teknis

| Layer | Teknologi | Folder |
|-------|-----------|--------|
| Frontend | Svelte 4 + Vite + TailwindCSS | `mini-helpdesk-web/` |
| HTTP Client | Axios | `mini-helpdesk-web/src/api/` |
| Backend | Java 21 + Spring Boot 3.3 | `mini-helpdesk-service/` |
| ORM | Spring Data JPA + Hibernate | `mini-helpdesk-service/src/.../repository/` |
| Database | PostgreSQL | port 5432, db: `bjb_helpdesk_db` |
| Auth | Spring Security + JWT stateless (access + refresh token) | `mini-helpdesk-service/src/.../security/` |

---

## 📁 Struktur Folder

### Backend (`mini-helpdesk-service/`)

```
src/main/java/com/bjb/helpdesk/
├── config/             → SecurityConfig
├── controller/         → @RestController per modul
├── dto/
│   ├── ApiResponse.java → wrapper { success, message, data }
│   ├── request/         → LoginRequest, RegisterRequest, RefreshTokenRequest
│   └── response/        → AuthResponse
├── exception/          → GlobalExceptionHandler
├── model/              → User, RefreshToken
├── repository/         → UserRepository, RefreshTokenRepository
├── security/           → JwtAuthFilter, JwtService
├── service/            → AuthService
└── HelpdeskApplication.java
```

### Frontend (`mini-helpdesk-web/`)

```
src/
├── api/                → axios instance + fungsi per domain
│   └── axios.js        → base config, interceptors, token injection
├── lib/                → komponen UI reusable (Svelte)
├── routes/             → halaman (Svelte routing via vite)
├── stores/             → Svelte stores (state global)
├── App.svelte          → root component
└── main.js             → bootstrap app
```

---

## 🔑 Konsep Kunci

### 1. State Management — Svelte Stores

```js
// stores/auth.js
import { writable } from 'svelte/store'

export const token = writable(localStorage.getItem('token'))
export const user = writable(null)
```

### 2. API Layer — Axios

```js
// api/axios.js
import axios from 'axios'
import { get } from 'svelte/store'
import { token } from '../stores/auth'

const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL })

api.interceptors.request.use(config => {
  const t = get(token)
  if (t) config.headers.Authorization = `Bearer ${t}`
  return config
})

export default api
```

### 3. Auth Flow

```
Login → POST /api/auth/login → dapat access token + refresh token
     → simpan access token di localStorage + Svelte store
     → axios interceptor inject ke setiap request

Refresh → POST /api/auth/refresh → dapat access token baru

Logout → hapus token dari localStorage + store
       → redirect ke /login
```

### 4. Response Format BE

```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

Error:
```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

---

## 🚀 Environment Variables

### FE (`mini-helpdesk-web/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### BE (`mini-helpdesk-service/src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bjb_helpdesk_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
server:
  port: 8080
  servlet:
    context-path: /api
app:
  jwt:
    secret: ${JWT_SECRET:...}
    access-token-expiration: 900000      # 15 menit
    refresh-token-expiration: 604800000  # 7 hari
```

---

## 📝 Naming Conventions

### Backend
| Layer | Konvensi | Contoh |
|-------|----------|--------|
| Controller | `{Domain}Controller` | `AuthController`, `TicketController` |
| Service | `{Domain}Service` | `AuthService` |
| Repository | `{Domain}Repository` | `UserRepository` |
| Entity | PascalCase | `User`, `RefreshToken` |
| DTO Request | `{Action}{Domain}Request` | `LoginRequest`, `CreateTicketRequest` |
| DTO Response | `{Domain}Response` | `AuthResponse` |
| Endpoint | kebab-case | `/auth/login`, `/tickets` |

### Frontend
| Entitas | Konvensi | Contoh |
|---------|----------|--------|
| Page/Component | PascalCase | `LoginPage.svelte`, `TicketCard.svelte` |
| Store | camelCase | `auth.js`, `ticket.js` |
| API module | camelCase + `Api` suffix | `authApi.js`, `ticketApi.js` |

---

## 🚫 Pantangan

- Jangan hardcode URL API di FE — pakai `VITE_API_BASE_URL`
- Jangan expose JPA entity langsung dari controller — selalu pakai DTO
- Jangan simpan password/secret di kode — pakai environment variable
