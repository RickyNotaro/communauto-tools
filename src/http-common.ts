import axios from 'axios';
import { Capacitor, CapacitorCookies } from '@capacitor/core';

const TARGET_BASE = 'https://www.reservauto.net';
const isNative = Capacitor.isNativePlatform();

// VITE_NO_PROXY=true  →  skip Vite proxy in dev, behave like production
const useDevProxy = !isNative && import.meta.env.DEV && !import.meta.env.VITE_NO_PROXY;

// Detect if the Communauto Auth Bridge extension is installed.
// The extension injects CORS headers + cookies via declarativeNetRequest,
// so we can call the API directly without a proxy.
// On native, no detection needed — CapacitorHttp bypasses CORS.
let extensionDetected = false;
let detectionDone = useDevProxy || isNative;

const detectionPromise = (!useDevProxy && !isNative)
  ? (async () => {
      try {
        const resp = await fetch(
          `${TARGET_BASE}/WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles?BranchID=1&LanguageID=2`,
          { method: 'GET', mode: 'cors', cache: 'no-store' },
        );
        extensionDetected = resp.ok;
      } catch {
        extensionDetected = false;
      }
      detectionDone = true;
    })()
  : Promise.resolve();

// Public API client.
// Native: direct to API (CapacitorHttp handles CORS + cookies natively).
// Web: extension (direct) > Vite proxy (dev) > corsproxy.io (prod fallback).
const apiClient = axios.create({
  baseURL: isNative ? TARGET_BASE : (useDevProxy ? '/api' : TARGET_BASE),
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});

if (!isNative && !useDevProxy) {
  // Wait for extension detection, then fall back to corsproxy.io if needed
  apiClient.interceptors.request.use(async (config) => {
    if (!detectionDone) await detectionPromise;

    if (extensionDetected) {
      // Extension handles CORS + cookies via declarativeNetRequest.
      // Remove headers that would trigger a CORS preflight — the server
      // doesn't handle OPTIONS requests, so we must keep requests "simple".
      delete config.headers['Content-Type'];
      delete config.headers['X-WCF-Cookie'];
      return config;
    }

    const targetUrl = new URL(config.url!, TARGET_BASE);
    if (config.params) {
      for (const [k, v] of Object.entries(config.params as Record<string, unknown>)) {
        if (v !== undefined && v !== null) targetUrl.searchParams.set(k, String(v));
      }
      config.params = undefined;
    }
    config.baseURL = '';
    config.url = `https://corsproxy.io/?url=${encodeURIComponent(targetUrl.toString())}`;
    return config;
  });
}

// Authenticated REST API client — direct to restapifrontoffice
const restApi = axios.create({
  baseURL: 'https://restapifrontoffice.reservauto.net',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json; charset=utf-8',
  },
});

export function setRestApiToken(token: string | null) {
  if (token) {
    restApi.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete restApi.defaults.headers.common['Authorization'];
  }
}

// WCF cookies — on native, injected into the native cookie jar.
// On web, sent as X-WCF-Cookie header (converted to Cookie by Vite proxy or extension).
export async function setWcfCookies(cookies: string | null) {
  if (isNative) {
    if (cookies) {
      const pairs = cookies.split(';').map(s => s.trim()).filter(Boolean);
      for (const pair of pairs) {
        const [key, ...rest] = pair.split('=');
        await CapacitorCookies.setCookie({
          url: TARGET_BASE,
          key: key.trim(),
          value: rest.join('=').trim(),
        });
      }
    } else {
      await CapacitorCookies.clearCookies({ url: TARGET_BASE });
    }
  } else {
    if (cookies) {
      apiClient.defaults.headers.common['X-WCF-Cookie'] = cookies;
    } else {
      delete apiClient.defaults.headers.common['X-WCF-Cookie'];
    }
  }
}

export function isExtensionActive() {
  return extensionDetected;
}

export { restApi };
export default apiClient;
