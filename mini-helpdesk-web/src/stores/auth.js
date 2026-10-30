import { writable, get } from 'svelte/store'
import { authApi } from '../api/authApi'

const stored = JSON.parse(localStorage.getItem('auth') || 'null')

export const authStore = writable({
  user: stored?.user || null,
  token: stored?.token || null,
  refreshToken: stored?.refreshToken || null,
})

authStore.subscribe((val) => {
  if (val.token) localStorage.setItem('auth', JSON.stringify(val))
  else localStorage.removeItem('auth')
})

export async function login(credentials) {
  const res = await authApi.login(credentials)
  authStore.set({
    user: { username: res.data.username, role: res.data.role },
    token: res.data.accessToken,
    refreshToken: res.data.refreshToken,
  })
}

export async function logout() {
  try {
    await authApi.logout()
  } finally {
    authStore.set({ user: null, token: null, refreshToken: null })
  }
}

export async function refreshAccessToken() {
  const { refreshToken } = get(authStore)
  const res = await authApi.refresh(refreshToken)
  authStore.update((s) => ({ ...s, token: res.data.accessToken }))
}
