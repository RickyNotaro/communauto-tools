import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : 'https://corsproxy.io/?url=https://www.reservauto.net',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});

export default apiClient;
