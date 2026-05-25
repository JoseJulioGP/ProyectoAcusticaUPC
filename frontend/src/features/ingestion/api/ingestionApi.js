import apiClient from "../../../shared/api/apiClient";

export const ingestionApi = {
  upload(file, zoneId) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("zoneId", zoneId);
    return apiClient.post("/ingest/batches", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }).then((r) => r.data);
  },

  list({ page = 0, size = 20 } = {}) {
    return apiClient
      .get("/ingest/batches", { params: { page, size, sort: "uploadedAt,desc" } })
      .then((r) => r.data);
  },

  get(batchId) {
    return apiClient.get(`/ingest/batches/${batchId}`).then((r) => r.data);
  },

  measurements(batchId, { page = 0, size = 50 } = {}) {
    return apiClient
      .get(`/ingest/batches/${batchId}/measurements`, {
        params: { page, size, sort: "measuredAt,asc" },
      })
      .then((r) => r.data);
  },
};
