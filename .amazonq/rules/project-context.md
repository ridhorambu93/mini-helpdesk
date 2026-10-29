# 📌 Project Context: EMR App

---

## 🏛️ Domain & Tujuan

**Electronic Medical Record (EMR) — Sistem Informasi Klinik/Rumah Sakit**

Sistem fullstack untuk manajemen rekam medis, antrian, farmasi, laboratorium, billing, dan integrasi BPJS.

**Fungsi Utama:**
- Pendaftaran & manajemen pasien
- Antrian & kunjungan
- Rekam medis (SOAP, vital sign, triage)
- Farmasi, laboratorium, radiologi
- Billing & pembayaran
- Integrasi BPJS / SatuSehat

---

## 🏗️ Arsitektur Teknis

| Layer | Teknologi | Folder |
|-------|-----------|--------|
| Frontend | React 19 + Vite + TailwindCSS | `web-emr/` |
| State Management | React Context API | `web-emr/src/context/` |
| HTTP Client | Axios | `web-emr/src/api/` |
| Backend | Java 21 + Spring Boot 3.3 | `service-emr-api/` |
| ORM | Spring Data JPA + Hibernate | `service-emr-api/src/.../repository/` |
| Database | PostgreSQL | port 5432 |
| Auth | Spring Security + JWT (stateless) | `service-emr-api/src/.../config/` |

**Referensi lama (jangan dimodifikasi):**
- `be/` → Node.js + Express (referensi struktur modul)
- `fe/` → React + Zustand (referensi halaman & komponen)

---

## 📁 Struktur Folder

### Backend (`service-emr-api/`)

```
src/main/java/com/emr/api/
├── config/             → SecurityConfig, JwtConfig, CorsConfig
├── controller/         → @RestController per modul
│   └── emr/            → PatientController, VisitController, dll
├── dto/
│   ├── ApiResponse.java → wrapper { success, message, data }
│   ├── request/         → DTO input
│   └── response/        → DTO output
├── exception/          → GlobalExceptionHandler, custom exceptions
├── model/              → @Entity JPA
├── repository/         → @Repository extends JpaRepository
└── service/            → @Service business logic
    └── emr/
```

### Frontend (`web-emr/`)

```
src/
├── api/                → axios instance + fungsi per domain
│   ├── axios.js        → base config, interceptors, token injection
│   └── emr/            → patientApi.js, visitApi.js, dll
├── components/
│   ├── ui/             → Button, Input, Modal, Table, Badge, dll
│   └── shared/         → komponen domain-specific reusable
├── context/
│   ├── AuthContext.jsx → user, token, login(), logout()
│   ├── BranchContext.jsx
│   └── index.jsx       → AppProviders
├── hooks/              → usePatients(), useQueue(), dll
├── layouts/
│   ├── MainLayout.jsx  → Sidebar + Header + <Outlet />
│   └── AuthLayout.jsx  → layout login
├── pages/              → satu folder per modul
├── routes/
│   ├── AppRoutes.jsx
│   └── ProtectedRoute.jsx
└── utils/              → formatters, helpers
```

---

## 🔑 Konsep Kunci

### 1. State Management — React Context API

Tidak ada Zustand, tidak ada TanStack Query. Semua global state via Context:

```jsx
// context/AuthContext.jsx
const AuthContext = createContext()

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(localStorage.getItem("token"))

  const login = async (credentials) => { /* ... */ }
  const logout = () => { /* ... */ }

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
```

### 2. Data Fetching — Custom Hook + useEffect

```jsx
// hooks/usePatients.js
export function usePatients() {
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    patientApi.getAll()
      .then(setPatients)
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  return { patients, loading, error }
}
```

### 3. API Layer — Axios per Domain

```js
// api/axios.js
const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL })

api.interceptors.request.use(config => {
  const token = localStorage.getItem("token")
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default api
```

```js
// api/emr/patientApi.js
import api from "../axios"

export const patientApi = {
  getAll: (params) => api.get("/emr/patients", { params }).then(r => r.data),
  getById: (id) => api.get(`/emr/patients/${id}`).then(r => r.data),
  create: (data) => api.post("/emr/patients", data).then(r => r.data),
  update: (id, data) => api.put(`/emr/patients/${id}`, data).then(r => r.data),
}
```

### 4. Auth Flow

```
Login → POST /api/auth/login → dapat JWT token
     → simpan token di localStorage
     → set di AuthContext
     → axios interceptor inject ke setiap request

Logout → hapus token dari localStorage + AuthContext
       → redirect ke /login

ProtectedRoute → cek AuthContext.token
              → kalau null → redirect /login
```

### 5. Response Format BE

Semua response BE menggunakan `ApiResponse<T>`:

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
  "message": "Resource not found",
  "data": null
}
```

---

## 🚀 Environment Variables

### FE (`web-emr/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### BE (`service-emr-api/src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/emr_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
server:
  port: 8080
  servlet:
    context-path: /api
```

---

## 📝 Naming Conventions

### Backend
| Layer | Konvensi | Contoh |
|-------|----------|--------|
| Controller | `{Domain}Controller` | `PatientController` |
| Service | `{Domain}Service` | `PatientService` |
| Repository | `{Domain}Repository` | `PatientRepository` |
| Entity | PascalCase | `Patient`, `Visit` |
| DTO Request | `{Action}{Domain}Request` | `CreatePatientRequest` |
| DTO Response | `{Domain}Response` | `PatientResponse` |
| Endpoint | kebab-case | `/emr/patients`, `/emr/nursing-notes` |

### Frontend
| Entitas | Konvensi | Contoh |
|---------|----------|--------|
| Page | PascalCase + `Page` opsional | `PatientListPage.jsx` |
| Component | PascalCase | `PatientCard.jsx` |
| Hook | camelCase + `use` prefix | `usePatients.js` |
| API module | camelCase + `Api` suffix | `patientApi.js` |
| Context | PascalCase + `Context` | `AuthContext.jsx` |

---

## 🚫 Pantangan

- Jangan modifikasi folder `be/` dan `fe/` (referensi lama)
- Jangan hardcode URL API di FE — pakai `VITE_API_BASE_URL`
- Jangan expose JPA entity langsung dari controller — selalu pakai DTO
- Jangan simpan password/secret di kode — pakai environment variable
- Jangan buat state global baru di luar Context yang sudah ada
