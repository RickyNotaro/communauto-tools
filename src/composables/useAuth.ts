import { ref, computed, watch } from 'vue';
import { setRestApiToken, setWcfCookies } from '@/http-common';

const TOKEN_KEY = 'communauto_access_token';
const COOKIES_KEY = 'communauto_wcf_cookies';

const accessToken = ref<string | null>(sessionStorage.getItem(TOKEN_KEY));
const wcfCookies = ref<string | null>(sessionStorage.getItem(COOKIES_KEY));

const isAuthenticated = computed(() => !!wcfCookies.value || !!accessToken.value);

interface LoginCredentials {
  accessToken?: string;
  cookies?: string;
}

function login(creds: LoginCredentials) {
  if (creds.accessToken?.trim()) {
    accessToken.value = creds.accessToken.trim();
    sessionStorage.setItem(TOKEN_KEY, accessToken.value);
  }
  if (creds.cookies?.trim()) {
    wcfCookies.value = creds.cookies.trim();
    sessionStorage.setItem(COOKIES_KEY, wcfCookies.value);
  }
}

function logout() {
  accessToken.value = null;
  wcfCookies.value = null;
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(COOKIES_KEY);
}

// Sync tokens to axios whenever they change
watch(accessToken, (t) => setRestApiToken(t), { immediate: true });
watch(wcfCookies, (c) => setWcfCookies(c), { immediate: true });

export function useAuth() {
  return {
    accessToken,
    wcfCookies,
    isAuthenticated,
    login,
    logout,
  };
}
