import axios from 'axios';

const TARGET_BASE = 'https://www.reservauto.net';

// VITE_NO_PROXY=true  →  skip Vite proxy in dev, behave like production
const useDevProxy = import.meta.env.DEV && !import.meta.env.VITE_NO_PROXY;

// Detect if the Communauto Auth Bridge extension is installed.
// The extension injects CORS headers + cookies via declarativeNetRequest,
// so we can call the API directly without a proxy.
let extensionDetected = false;
let detectionDone = useDevProxy; // skip detection when using Vite proxy

const detectionPromise = !useDevProxy
  ? (async () => {
      try {
        // Simple GET with mode: 'cors' — if the extension is injecting
        // Access-Control-Allow-Origin headers, this will succeed.
        // Without the extension, it fails with a CORS error.
        // cache: 'no-store' prevents a cached corsproxy.io response from
        // giving a false positive.
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

// Public API client — uses extension (direct) > Vite proxy (dev) > corsproxy.io (prod fallback).
const apiClient = axios.create({
  baseURL: useDevProxy ? '/api' : TARGET_BASE,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});

if (!useDevProxy) {
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

// WCF cookies — sent as X-WCF-Cookie header, converted to Cookie by Vite proxy or extension
export function setWcfCookies(cookies: string | null) {
  if (cookies) {
    apiClient.defaults.headers.common['X-WCF-Cookie'] = cookies;
  } else {
    delete apiClient.defaults.headers.common['X-WCF-Cookie'];
  }
}

export function isExtensionActive() {
  return extensionDetected;
}

export { restApi };
export default apiClient;
