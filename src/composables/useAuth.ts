import { ref, computed, watch } from 'vue';
import { setRestApiToken, setWcfCookies } from '@/http-common';
import { checkHashForCredentials } from '@/composables/useOAuth';

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

/**
 * Check URL hash for credentials on load (from extension or bookmarklet redirect).
 * Returns true if credentials were found and applied.
 */
function tryLoginFromHash(): boolean {
  const creds = checkHashForCredentials();
  if (!creds) return false;

  if (creds.cookies) {
    wcfCookies.value = creds.cookies;
    sessionStorage.setItem(COOKIES_KEY, creds.cookies);
  }
  if (creds.token) {
    accessToken.value = creds.token;
    sessionStorage.setItem(TOKEN_KEY, creds.token);
  }

  return true;
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
    tryLoginFromHash,
    logout,
  };
}
