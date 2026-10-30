import axios from 'axios'
import {authStore} from '../stores/auth'
import {get} from 'svelte/store'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

api.interceptors.request.use((config) => {
  const token = get(authStore).token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
