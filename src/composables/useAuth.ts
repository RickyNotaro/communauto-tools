import { ref, computed, watch } from 'vue';
import { Capacitor } from '@capacitor/core';
import { Preferences } from '@capacitor/preferences';
import { setRestApiToken, setWcfCookies } from '@/http-common';
import { checkHashForCredentials } from '@/composables/useOAuth';

const isNative = Capacitor.isNativePlatform();

const TOKEN_KEY = 'communauto_access_token';
const COOKIES_KEY = 'communauto_wcf_cookies';

const accessToken = ref<string | null>(sessionStorage.getItem(TOKEN_KEY));
const wcfCookies = ref<string | null>(sessionStorage.getItem(COOKIES_KEY));

// On native, also check if cookies exist in the native cookie jar
// (set by in-app login flow). We track this separately.
const nativeAuthenticated = ref(false);

const isAuthenticated = computed(
  () => !!wcfCookies.value || !!accessToken.value || nativeAuthenticated.value,
);

interface LoginCredentials {
  accessToken?: string;
  cookies?: string;
}

function login(creds: LoginCredentials) {
  if (creds.accessToken?.trim()) {
    accessToken.value = creds.accessToken.trim();
    sessionStorage.setItem(TOKEN_KEY, accessToken.value);
    if (isNative) Preferences.set({ key: TOKEN_KEY, value: accessToken.value });
  }
  if (creds.cookies?.trim()) {
    wcfCookies.value = creds.cookies.trim();
    sessionStorage.setItem(COOKIES_KEY, wcfCookies.value);
    if (isNative) Preferences.set({ key: COOKIES_KEY, value: wcfCookies.value });
  }
}

async function logout() {
  accessToken.value = null;
  wcfCookies.value = null;
  nativeAuthenticated.value = false;
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(COOKIES_KEY);
  if (isNative) {
    await Preferences.remove({ key: TOKEN_KEY });
    await Preferences.remove({ key: COOKIES_KEY });
  }
  await setWcfCookies(null);
}

/**
 * Mark as authenticated after native in-app login.
 * The native cookie jar already has the cookies — no need to store them manually.
 */
function setNativeAuthenticated() {
  nativeAuthenticated.value = true;
}

/**
 * Check URL hash for credentials on load (from extension redirect).
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

/** Restore persisted auth on native startup */
async function initNativeAuth() {
  if (!isNative) return;
  const token = await Preferences.get({ key: TOKEN_KEY });
  const cookies = await Preferences.get({ key: COOKIES_KEY });
  if (token.value) {
    accessToken.value = token.value;
  }
  if (cookies.value) {
    wcfCookies.value = cookies.value;
  }
}

// Sync tokens to axios whenever they change
watch(accessToken, (t) => setRestApiToken(t), { immediate: true });
watch(wcfCookies, (c) => { setWcfCookies(c); }, { immediate: true });

export function useAuth() {
  return {
    accessToken,
    wcfCookies,
    isAuthenticated,
    isNative,
    login,
    tryLoginFromHash,
    setNativeAuthenticated,
    initNativeAuth,
    logout,
  };
}
