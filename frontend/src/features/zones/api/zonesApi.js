import apiClient from "../../../shared/api/apiClient";

export const zonesApi = {
  list() {
    return apiClient.get("/api/v1/zones").then((r) => r.data);
  },
};