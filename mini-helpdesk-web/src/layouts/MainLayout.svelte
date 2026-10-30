<script>
  import { authStore, logout } from '../stores/auth'
</script>

<div class="flex h-screen bg-gray-100">
  <aside class="w-64 bg-white shadow-md">
    <div class="p-4 font-bold text-lg text-blue-600">BJB Helpdesk</div>
    <nav class="mt-4 text-sm text-gray-600 px-4 space-y-2">
      <a href="/" class="block hover:text-blue-600">Dashboard</a>
    </nav>
  </aside>
  <div class="flex-1 flex flex-col overflow-hidden">
    <header class="bg-white shadow-sm px-6 py-4 flex justify-between items-center">
      <span class="text-gray-700 font-medium">Selamat datang, {$authStore.user?.username}</span>
      <button on:click={logout} class="text-sm text-red-500 hover:underline">Logout</button>
    </header>
    <main class="flex-1 overflow-auto p-6">
      <slot />
    </main>
  </div>
</div>