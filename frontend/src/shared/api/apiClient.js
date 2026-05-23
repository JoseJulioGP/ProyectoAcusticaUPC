import axios from 'axios';

const STORAGE_KEY = 'acusticupc.token';

const apiClient = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem(STORAGE_KEY);
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const tokenStorage = {
  get: () => localStorage.getItem(STORAGE_KEY),
  set: (token) => localStorage.setItem(STORAGE_KEY, token),
  clear: () => localStorage.removeItem(STORAGE_KEY),
};

export default apiClient;
