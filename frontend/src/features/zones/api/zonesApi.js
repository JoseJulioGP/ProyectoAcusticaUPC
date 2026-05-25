import apiClient from "../../../shared/api/apiClient";

export const zonesApi = {
  list() {
    return apiClient.get("/zones").then((r) => r.data);
  },
};