import axios from 'axios';

const STORAGE_KEY = 'acusticupc.token';

/**
 * Token storage con opción "recordar sesión":
 *  - remember=true  → localStorage (persiste tras cerrar el navegador)
 *  - remember=false → sessionStorage (se borra al cerrar la pestaña)
 * `get` lee de ambos; `clear` limpia ambos.
 */
export const tokenStorage = {
  get: () => localStorage.getItem(STORAGE_KEY) ?? sessionStorage.getItem(STORAGE_KEY),
  set: (token, remember = true) => {
    if (remember) {
      localStorage.setItem(STORAGE_KEY, token);
      sessionStorage.removeItem(STORAGE_KEY);
    } else {
      sessionStorage.setItem(STORAGE_KEY, token);
      localStorage.removeItem(STORAGE_KEY);
    }
  },
  clear: () => {
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
  },
};

const apiClient = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      tokenStorage.clear();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
