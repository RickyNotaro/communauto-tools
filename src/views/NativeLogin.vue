<template>
  <div class="native-login">
    <div class="login-prompt">
      <h3>Connexion Communauto</h3>
      <p>Connectez-vous avec votre compte Communauto.</p>
      <p class="text-muted small">
        Vous serez redirige vers le site de Communauto.
        Apres la connexion, utilisez le bouton retour pour revenir ici.
      </p>

      <button class="btn btn-primary btn-lg mb-3" @click="startLogin">
        Se connecter
      </button>

      <div v-if="checking" class="text-muted small">Verification de la connexion...</div>
      <div v-if="authenticated" class="text-success small mb-2">
        Connecte!
      </div>

      <div v-if="cookieStatus" class="cookie-checklist mb-3">
        <h6>Cookies requis</h6>
        <ul class="list-unstyled mb-0">
          <li v-for="c in cookieStatus" :key="c.name" :class="c.present ? 'text-success' : 'text-danger'">
            {{ c.present ? '\u2705' : '\u274C' }} <code>{{ c.name }}</code>
            <span v-if="c.present && c.preview" class="text-muted small"> = {{ c.preview }}</span>
          </li>
        </ul>
      </div>

      <router-link to="/" class="btn btn-link mt-2">Retour</router-link>
      <router-link v-if="authenticated" to="/radar" class="btn btn-success mt-2">Aller au Radar</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Capacitor, registerPlugin } from '@capacitor/core';
import { useAuth } from '@/composables/useAuth';
import apiClient from '@/http-common';

interface WebViewCookiePlugin {
  getCookies(opts: { url: string }): Promise<{ cookies: string }>;
}
const WebViewCookie = registerPlugin<WebViewCookiePlugin>('WebViewCookie');

const { login, setNativeAuthenticated } = useAuth();

const checking = ref(false);
const authenticated = ref(false);

interface CookieEntry {
  name: string;
  present: boolean;
  preview?: string;
}
const cookieStatus = ref<CookieEntry[] | null>(null);

const REQUIRED_COOKIES = ['uid', 'bid', 'mySession'];

function parseCookies(raw: string): Record<string, string> {
  const map: Record<string, string> = {};
  for (const pair of raw.split(';')) {
    const trimmed = pair.trim();
    if (!trimmed) continue;
    const eq = trimmed.indexOf('=');
    if (eq === -1) continue;
    map[trimmed.slice(0, eq).trim()] = trimmed.slice(eq + 1).trim();
  }
  return map;
}

function buildCookieStatus(raw: string): CookieEntry[] {
  const parsed = parseCookies(raw);
  return REQUIRED_COOKIES.map((name) => {
    const value = parsed[name];
    return {
      name,
      present: value !== undefined && value !== '',
      preview: value ? (value.length > 20 ? value.slice(0, 20) + '...' : value) : undefined,
    };
  });
}

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
          cookieStatus.value = buildCookieStatus(result.cookies);
        }
      }

      // No auto-redirect — let the user check cookies and navigate manually
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

.cookie-checklist {
  text-align: left;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem 1.25rem;
  min-width: 240px;
}

.cookie-checklist h6 {
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.cookie-checklist li {
  padding: 0.15rem 0;
}
</style>
