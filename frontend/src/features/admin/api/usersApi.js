import apiClient from "../../../shared/api/apiClient";

export const usersApi = {
  list: (page = 0, size = 20) =>
    apiClient.get(`/users?page=${page}&size=${size}`).then((r) => r.data),

  create: (payload) =>
    apiClient.post("/users", payload).then((r) => r.data),

  update: (id, payload) =>
    apiClient.put(`/users/${id}`, payload).then((r) => r.data),

  resetPassword: (id, newPassword) =>
    apiClient.patch(`/users/${id}/password`, { newPassword }),

  deactivate: (id) =>
    apiClient.delete(`/users/${id}`),
};
