# Dokumen Analisa: EMR App (BE + FE)

---

## 1. Gambaran Umum

Proyek ini adalah **Electronic Medical Record (EMR)** — sistem informasi klinik/rumah sakit yang mencakup manajemen pasien, antrian, rekam medis, farmasi, laboratorium, radiologi, billing, dan integrasi eksternal (BPJS).

| Aspek | Detail |
|---|---|
| Nama Proyek | EMR App |
| Tipe | Fullstack Web App (SPA + REST API) |
| Backend | Java 21 + Spring Boot 3.3 + Spring Data JPA + PostgreSQL |
| Frontend | React 19 + Vite + TailwindCSS + React Context API |
| Folder BE | `service-emr-api/` |
| Folder FE | `web-emr/` |
| Referensi Lama | `be/` (Node.js), `fe/` (React + Zustand) — jangan dimodifikasi |
| Port BE | 8080 |
| Port FE | 5173 (dev) |

---

## 2. Backend (BE) — `service-emr-api/`

### 2.1 Teknologi Utama

| Dependency | Fungsi |
|---|---|
| spring-boot-starter-web | REST API (HTTP framework) |
| spring-boot-starter-data-jpa | ORM via Hibernate + PostgreSQL |
| spring-boot-starter-security | Auth & authorization |
| spring-boot-starter-validation | Validasi request (`@Valid`, `@NotNull`, dll) |
| postgresql | JDBC driver |
| lombok | Boilerplate reduction (`@Data`, `@Builder`, dll) |
| jjwt (rencana) | JWT token generation & validation |

### 2.2 Struktur Folder BE

```
service-emr-api/src/main/java/com/emr/api/
├── EmrApiApplication.java      → Entry point (@SpringBootApplication)
├── config/
│   ├── SecurityConfig.java     → Spring Security + CORS + JWT filter
│   └── (JwtConfig, dll)
├── controller/                 → @RestController per resource
│   └── emr/                    → PatientController, VisitController, dll
├── dto/
│   ├── ApiResponse.java        → Wrapper response { success, message, data }
│   ├── request/                → DTO untuk request body
│   └── response/               → DTO untuk response
├── exception/
│   ├── GlobalExceptionHandler.java  → @ControllerAdvice
│   └── ResourceNotFoundException.java
├── model/                      → @Entity JPA models
├── repository/                 → @Repository (JpaRepository)
└── service/                    → @Service business logic
    └── emr/                    → PatientService, VisitService, dll
```

### 2.3 Arsitektur & Pola

- **Controller → Service → Repository**: pola standar Spring Boot, tidak ada bypass layer.
- **DTO pattern**: request dan response selalu pakai DTO terpisah, bukan expose entity langsung.
- **ApiResponse wrapper**: semua response dibungkus `ApiResponse<T>` dengan format `{ success, message, data }`.
- **GlobalExceptionHandler**: semua exception ditangkap di satu tempat via `@ControllerAdvice`.
- **Stateless auth**: Spring Security dengan `SessionCreationPolicy.STATELESS`, JWT di header `Authorization: Bearer <token>`.
- **Soft delete**: gunakan field `deletedAt` atau `@SQLDelete` + `@Where` di entity.

### 2.4 Modul EMR yang Direncanakan

| Modul | Endpoint Prefix |
|---|---|
| Auth | `/api/auth` |
| Pasien | `/api/emr/patients` |
| Kunjungan | `/api/emr/visits` |
| Antrian | `/api/emr/queue` |
| Triage | `/api/emr/triage` |
| Catatan Keperawatan | `/api/emr/nursing-notes` |
| Riwayat Medis | `/api/emr/histories` |
| Poli | `/api/emr/polis` |
| Laboratorium | `/api/laboratory/orders`, `/results` |
| Radiologi | `/api/radiology/orders`, `/results` |
| Farmasi | `/api/pharmacy` |
| Billing | `/api/billing` |
| Pembayaran | `/api/payments` |
| BPJS Bridge | `/api/bridge/bpjs/...` |

### 2.5 Konfigurasi (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/emr_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8080
  servlet:
    context-path: /api
```

---

## 3. Frontend (FE) — `web-emr/`

### 3.1 Teknologi Utama

| Package | Fungsi |
|---|---|
| react 19 | UI library |
| react-router-dom v7 | Routing SPA |
| vite | Build tool |
| tailwindcss | Styling |
| axios | HTTP client |
| react-hook-form + zod | Form + validasi |
| sonner | Toast notification |
| React Context API | Global state management |

### 3.2 State Management: React Context API

Tidak menggunakan Zustand atau TanStack Query. Semua global state dikelola via React Context:

| Context | Isi |
|---|---|
| `AuthContext` | user, token, login, logout |
| `BranchContext` | cabang/poli aktif |
| `QueueContext` | data antrian aktif |
| (tambah sesuai kebutuhan) | |

Pola data fetching: custom hook + `useEffect` + `axios`.

### 3.3 Struktur Folder FE (Rencana)

```
web-emr/src/
├── api/                    → axios instance + endpoint functions per domain
│   ├── axios.js            → base axios config (baseURL, interceptors, token)
│   └── emr/                → patientApi.js, visitApi.js, queueApi.js, dll
├── components/
│   ├── ui/                 → Button, Input, Modal, Table, Badge, dll
│   └── shared/             → komponen reusable domain-specific
├── context/
│   ├── AuthContext.jsx     → auth state + JWT
│   ├── BranchContext.jsx   → cabang aktif
│   └── index.jsx           → AppProviders (gabung semua context)
├── hooks/                  → custom hooks (usePatients, useQueue, dll)
├── layouts/
│   ├── MainLayout.jsx      → Sidebar + Header + Outlet
│   └── AuthLayout.jsx      → layout untuk halaman login
├── pages/
│   ├── auth/               → Login
│   ├── dashboard/          → Overview, queue-monitoring
│   ├── medical-service/    → SOAP, vital sign, prescription
│   ├── patient-registration/ → Pendaftaran pasien
│   ├── pharmacy/           → Farmasi
│   ├── laboratory/         → Lab orders & results
│   ├── emergency-room/     → IGD
│   ├── inpatient/          → Rawat inap
│   ├── billing/            → Billing & invoice
│   └── settings/           → User, role, branch
├── routes/
│   ├── AppRoutes.jsx       → BrowserRouter + route definitions
│   └── ProtectedRoute.jsx  → cek auth sebelum render
└── utils/                  → formatters, validators, helpers
```

### 3.4 Routing Modul

| Path | Modul |
|---|---|
| `/login` | Autentikasi |
| `/dashboard/*` | Dashboard & monitoring |
| `/medical-service/*` | Layanan medis |
| `/patient-registration/*` | Pendaftaran pasien |
| `/pharmacy/*` | Farmasi |
| `/laboratory/*` | Laboratorium |
| `/emergency-room/*` | IGD |
| `/inpatient/*` | Rawat inap |
| `/billing/*` | Billing |
| `/settings/*` | Pengaturan sistem |

---

## 4. Kesesuaian BE ↔ FE

| FE Butuh | BE Endpoint | Status |
|---|---|---|
| Login | `POST /api/auth/login` | 🔲 Belum |
| Data pasien | `GET /api/emr/patients` | 🔲 Belum |
| Antrian hari ini | `GET /api/emr/queue/today` | 🔲 Belum |
| SOAP | `POST /api/emr/visits/{id}/soap` | 🔲 Belum |
| Riwayat pasien | `GET /api/emr/histories/{patientId}` | 🔲 Belum |

---

## 5. Rekomendasi Prioritas

### 🔴 Fondasi (Harus Ada Dulu)
1. BE: Setup JWT auth (`/api/auth/login`, `/api/auth/me`)
2. BE: Model + endpoint Pasien & Kunjungan
3. FE: Setup project Vite + struktur folder
4. FE: `AuthContext` + axios interceptor + `ProtectedRoute`
5. FE: Layout utama (Sidebar + Header)

### 🟡 Core EMR
6. BE + FE: Modul Antrian
7. BE + FE: Modul Rekam Medis (SOAP, vital sign)
8. BE + FE: Modul Farmasi

### 🟢 Extended
9. BE + FE: Laboratorium & Radiologi
10. BE + FE: Billing & Pembayaran
11. BE + FE: Integrasi BPJS

---

## 6. Ringkasan Arsitektur

```
[Browser]
    │
    ▼
[FE: React 19 + Vite]  port 5173
    │  axios + React Context API
    ▼
[BE: Java Spring Boot 3.3]  port 8080
    │  context-path: /api
    ▼
[PostgreSQL]  port 5432
```
