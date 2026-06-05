import apiClient from '../../../shared/api/apiClient';

export const authApi = {
  login: async (email, password) => {
    const { data } = await apiClient.post('/auth/login', { email, password });
    return data;
  },

  me: async () => {
    const { data } = await apiClient.get('/auth/me');
    return data;
  },

  // Registro público. El backend fuerza rol VIEWER: NO enviar `role`.
  register: async ({ email, password, fullName }) => {
    const { data } = await apiClient.post('/auth/register', {
      email,
      password,
      fullName,
    });
    return data;
  },
};