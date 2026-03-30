<template>
  <div class="native-login">
    <div class="login-prompt">
      <h3>Connexion Communauto</h3>
      <p>Connectez-vous avec votre compte Communauto.</p>
      <p class="text-muted small">
        Vous serez redirigé vers le site de Communauto.
        Après la connexion, utilisez le bouton retour pour revenir ici.
      </p>

      <button class="btn btn-primary btn-lg mb-3" @click="startLogin">
        Se connecter
      </button>

      <div v-if="checking" class="text-muted small">Vérification de la connexion...</div>
      <div v-if="authenticated" class="text-success small">
        Connecté! Redirection...
      </div>

      <router-link to="/" class="btn btn-link mt-2">Retour</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Capacitor, registerPlugin } from '@capacitor/core';
import { useAuth } from '@/composables/useAuth';
import apiClient from '@/http-common';

interface WebViewCookiePlugin {
  getCookies(opts: { url: string }): Promise<{ cookies: string }>;
}
const WebViewCookie = registerPlugin<WebViewCookiePlugin>('WebViewCookie');

const router = useRouter();
const { login, setNativeAuthenticated } = useAuth();

const checking = ref(false);
const authenticated = ref(false);

onMounted(async () => {
  // Always check for cookies on mount — handles the case where
  // user navigated to reservauto.net, logged in, and pressed back.
  await checkLogin();
});

function startLogin() {
  // Navigate the WebView to the Communauto login page.
  // Same WebView = same cookie jar. After login, user presses
  // Android back button to return to the app.
  window.location.href = 'https://quebec.client.reservauto.net/bookCar';
}

async function checkLogin() {
  checking.value = true;
  try {
    const resp = await apiClient.get(
      '/WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles',
      { params: { BranchID: 1, LanguageID: 2 }, validateStatus: () => true },
    );

    if (resp.status === 200 && resp.data) {
      authenticated.value = true;
      setNativeAuthenticated();

      // Read cookies from Android's CookieManager (WebView cookie jar)
      // and store them in useAuth so getUidFromCookies() can find the uid.
      if (Capacitor.isNativePlatform()) {
        const result = await WebViewCookie.getCookies({ url: 'https://www.reservauto.net' });
        if (result.cookies) {
          login({ cookies: result.cookies });
        }
      }

      router.replace('/radar');
      return;
    }
  } catch {
    // Not authenticated yet
  }
  checking.value = false;
}
</script>

<style scoped>
.native-login {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 2rem;
  text-align: center;
}

.login-prompt p {
  color: #666;
  margin: 0.5rem 0;
  max-width: 320px;
}
</style>
