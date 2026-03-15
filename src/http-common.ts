import axios from 'axios';

const TARGET_BASE = 'https://www.reservauto.net';

// Public API client — proxied through Vite in dev, corsproxy.io in prod.
const apiClient = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : TARGET_BASE,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});

if (!import.meta.env.DEV) {
  apiClient.interceptors.request.use((config) => {
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

// WCF cookies — sent as X-WCF-Cookie header, converted to Cookie by Vite proxy
export function setWcfCookies(cookies: string | null) {
  if (cookies) {
    apiClient.defaults.headers.common['X-WCF-Cookie'] = cookies;
  } else {
    delete apiClient.defaults.headers.common['X-WCF-Cookie'];
  }
}

export { restApi };
export default apiClient;
