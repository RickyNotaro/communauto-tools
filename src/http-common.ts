import axios from 'axios';

// Public API client — proxied through Vite in dev
const apiClient = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : 'https://www.reservauto.net',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});

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
