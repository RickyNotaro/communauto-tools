<template>
  <div class="callback-container">
    <p v-if="authenticated">Connecté! Redirection…</p>
    <p v-else>Connexion échouée. <router-link to="/">Retour</router-link></p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';

const router = useRouter();
const { tryLoginFromHash, isAuthenticated } = useAuth();
const authenticated = ref(false);

onMounted(() => {
  tryLoginFromHash();
  authenticated.value = isAuthenticated.value;
  if (authenticated.value) {
    router.replace('/radar');
  }
});
</script>

<style scoped>
.callback-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  font-family: sans-serif;
}
</style>
