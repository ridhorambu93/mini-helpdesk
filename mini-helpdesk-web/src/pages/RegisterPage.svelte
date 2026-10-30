<script>
  import { authApi } from '../api/authApi'

  let username = ''
  let email = ''
  let password = ''
  let loading = false
  let error = ''
  let success = ''

  async function handleSubmit() {
    loading = true
    error = ''
    success = ''
    try {
      await authApi.register({ username, email, password })
      success = 'Registrasi berhasil! Silakan login.'
      username = ''
      email = ''
      password = ''
    } catch (e) {
      error = e.response?.data?.message || 'Registrasi gagal'
    } finally {
      loading = false
    }
  }
</script>

<div class="min-h-screen flex items-center justify-center bg-gray-100">
  <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
    <h1 class="text-2xl font-bold text-blue-600 text-center mb-6">Daftar Akun</h1>

    {#if error}
      <p class="text-red-500 text-sm mb-4">{error}</p>
    {/if}
    {#if success}
      <p class="text-green-500 text-sm mb-4">{success}</p>
    {/if}

    <form on:submit|preventDefault={handleSubmit} class="space-y-4">
      <input bind:value={username} type="text" placeholder="Username"
        class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
      <input bind:value={email} type="email" placeholder="Email"
        class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
      <input bind:value={password} type="password" placeholder="Password (min. 6 karakter)"
        class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
      <button type="submit" disabled={loading}
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white py-2 rounded text-sm font-medium transition">
        {loading ? 'Memproses...' : 'Daftar'}
      </button>
    </form>
  </div>
</div>
