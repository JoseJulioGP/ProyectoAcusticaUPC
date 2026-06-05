import apiClient from "../../../shared/api/apiClient";

export const zonesApi = {
  list() {
    return apiClient.get("/zones").then((r) => r.data);
  },
  get(id) {
    return apiClient.get(`/zones/${id}`).then((r) => r.data);
  },
  create(body) {
    return apiClient.post("/zones", body).then((r) => r.data);
  },
  update(id, body) {
    return apiClient.put(`/zones/${id}`, body).then((r) => r.data);
  },
  // soft delete por defecto (active=false); hard=true intenta borrado físico.
  remove(id, hard = false) {
    return apiClient
      .delete(`/zones/${id}`, { params: { hard } })
      .then((r) => r.data);
  },
};
