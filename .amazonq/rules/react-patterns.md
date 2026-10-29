# ⚛️ React Patterns: EMR App (web-emr)

---

## 🏗️ Konvensi Penamaan

| Entitas | Konvensi | Contoh |
|---------|----------|--------|
| Pages | PascalCase | `PatientListPage.jsx`, `LoginPage.jsx` |
| Components | PascalCase | `PatientCard.jsx`, `QueueTable.jsx` |
| Hooks | camelCase + `use` | `usePatients.js`, `useQueue.js` |
| Context | PascalCase + `Context` | `AuthContext.jsx` |
| API modules | camelCase + `Api` | `patientApi.js` |
| Utils | camelCase | `formatDate.js`, `validators.js` |

---

## 🌐 State Management — React Context API

### Membuat Context

```jsx
// context/AuthContext.jsx
import { createContext, useContext, useState } from "react"

const AuthContext = createContext()

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(() => localStorage.getItem("token"))

  const login = (userData, jwt) => {
    setUser(userData)
    setToken(jwt)
    localStorage.setItem("token", jwt)
  }

  const logout = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem("token")
  }

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
```

### Menggabungkan Semua Provider

```jsx
// context/index.jsx
export function AppProviders({ children }) {
  return (
    <AuthProvider>
      <BranchProvider>
        {children}
      </BranchProvider>
    </AuthProvider>
  )
}
```

```jsx
// main.jsx
<AppProviders>
  <App />
</AppProviders>
```

---

## 🪝 Custom Hooks untuk Data Fetching

Pola standar: custom hook + `useEffect` + `useState`.

```jsx
// hooks/usePatients.js
import { useState, useEffect } from "react"
import { patientApi } from "../api/emr/patientApi"

export function usePatients(params) {
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    patientApi.getAll(params)
      .then(res => setPatients(res.data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  return { patients, loading, error }
}
```

Penggunaan di komponen:

```jsx
function PatientListPage() {
  const { patients, loading, error } = usePatients()

  if (loading) return <div>Loading...</div>
  if (error) return <div>Error: {error}</div>

  return <PatientTable data={patients} />
}
```

---

## 🔔 Toast Notifications — Sonner

```jsx
import { toast } from "sonner"

// Sukses
toast.success("Pasien berhasil disimpan")

// Error
toast.error("Gagal menyimpan data")

// Loading async
const id = toast.loading("Menyimpan...")
// setelah selesai:
toast.dismiss(id)
toast.success("Berhasil!")
```

Global `<Toaster />` dipasang di `App.jsx` atau `main.jsx`.

---

## 🧭 Routing & Navigation

```jsx
import { useNavigate, Link, useParams } from "react-router-dom"

// Navigasi programatik
const navigate = useNavigate()
navigate("/patient-registration")

// Link
<Link to="/dashboard">Dashboard</Link>

// Params
const { id } = useParams()
```

---

## 🔐 Auth & Protected Routes

```jsx
// routes/ProtectedRoute.jsx
import { Navigate } from "react-router-dom"
import { useAuth } from "../context/AuthContext"

export function ProtectedRoute({ children }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return children
}
```

```jsx
// routes/AppRoutes.jsx
<Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
  <Route path="/dashboard" element={<DashboardPage />} />
  <Route path="/patient-registration" element={<PatientRegistrationPage />} />
</Route>
```

---

## 📄 Layout Pattern

```jsx
// layouts/MainLayout.jsx
import { Outlet } from "react-router-dom"

export function MainLayout() {
  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Header />
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

---

## 📝 Form Pattern — React Hook Form + Zod

```jsx
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"

const schema = z.object({
  name: z.string().min(1, "Nama wajib diisi"),
  nik: z.string().length(16, "NIK harus 16 digit"),
})

export function PatientForm({ onSubmit }) {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema)
  })

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register("name")} />
      {errors.name && <span>{errors.name.message}</span>}
    </form>
  )
}
```

---

## 🚫 Anti-Patterns

- Jangan fetch data langsung di body komponen tanpa custom hook
- Jangan simpan token di sessionStorage — pakai localStorage
- Jangan pakai `window.location.href` untuk navigasi — pakai `useNavigate`
- Jangan buat Context baru untuk state yang cukup dengan `useState` lokal
- Jangan hardcode URL API — pakai `VITE_API_BASE_URL` via axios instance
