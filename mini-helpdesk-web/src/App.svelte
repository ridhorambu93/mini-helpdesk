<script>
  import { authStore } from './stores/auth'
  import LoginPage from './pages/LoginPage.svelte'
  import MainLayout from './layouts/MainLayout.svelte'
</script>

{#if $authStore.token}
  <MainLayout />
{:else}
  <LoginPage />
{/if}
