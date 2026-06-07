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

  // Cambio de contraseña desde el LOGIN (sin sesión previa): se valida la
  // contraseña actual haciendo login para obtener un token, y con ese token se
  // llama al endpoint protegido /auth/change-password. No persiste el token.
  changePasswordWithCredentials: async ({ identifier, currentPassword, newPassword }) => {
    const { data } = await apiClient.post('/auth/login', {
      email: identifier,
      password: currentPassword,
    });
    await apiClient.patch(
      '/auth/change-password',
      { currentPassword, newPassword },
      { headers: { Authorization: `Bearer ${data.token}` } }
    );
  },
};