import apiClient from "../../../shared/api/apiClient";

export const complianceApi = {
  
  evaluateBatch(batchId) {
    return apiClient
      .post(`/api/v1/compliance/evaluate/${batchId}`)
      .then((r) => r.data);
  },

  
  getByBatch(batchId) {
    return apiClient
      .get(`/api/v1/compliance/results/batch/${batchId}`)
      .then((r) => r.data);
  },

   
  listResults({ zoneId, from, to, page = 0, size = 20 }) {
    return apiClient
      .get("/api/v1/compliance/results", {
        params: { zoneId, from, to, page, size, sort: "evaluatedAt,desc" },
      })
      .then((r) => r.data);
  },


  listAlerts({ zoneId, from, to, page = 0, size = 20 } = {}) {
    const params = { from, to, page, size, sort: "triggeredAt,desc" };
    if (zoneId) params.zoneId = zoneId;
    return apiClient
      .get("/api/v1/compliance/alerts", { params })
      .then((r) => r.data);
  },
};
