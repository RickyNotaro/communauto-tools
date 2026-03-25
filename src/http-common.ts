import axios from 'axios';

const TARGET_BASE = 'https://www.reservauto.net';

// Detect if the Communauto Auth Bridge extension is installed.
// The extension injects CORS headers + cookies via declarativeNetRequest,
// so we can call the API directly without a proxy.
let extensionDetected = false;
let detectionDone = false;

const detectionPromise = !import.meta.env.DEV
  ? (async () => {
      try {
        const resp = await fetch(
          `${TARGET_BASE}/WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles?BranchID=1&LanguageID=2`,
          { method: 'HEAD', mode: 'cors' },
        );
        extensionDetected = resp.ok;
      } catch {
        extensionDetected = false;
      }
      detectionDone = true;
    })()
  : Promise.resolve();

// Public API client — proxied through Vite in dev, direct with extension, corsproxy.io as fallback.
const apiClient = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : TARGET_BASE,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});

if (!import.meta.env.DEV) {
  // In production, wait for extension detection then decide proxy vs direct
  apiClient.interceptors.request.use(async (config) => {
    if (!detectionDone) await detectionPromise;

    if (extensionDetected) {
      // Extension handles CORS + cookies — call API directly
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
