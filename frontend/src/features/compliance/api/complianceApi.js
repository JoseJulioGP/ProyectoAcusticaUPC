import apiClient from "../../../shared/api/apiClient";

export const complianceApi = {

  evaluateBatch(batchId) {
    return apiClient
      .post(`/compliance/evaluate/${batchId}`)
      .then((r) => r.data);
  },

  getByBatch(batchId) {
    return apiClient
      .get(`/compliance/results/batch/${batchId}`)
      .then((r) => r.data);
  },

  listResults({ zoneId, from, to, page = 0, size = 20 }) {
    return apiClient
      .get("/compliance/results", {
        params: { zoneId, from, to, page, size, sort: "evaluatedAt,desc" },
      })
      .then((r) => r.data);
  },

  // Endpoint paginado nuevo (RF-12) → AlertController. Actualmente da 500 en backend,
  // por eso AlertsPage usa listAlerts (/compliance/alerts) como workaround.
  listAlertsPaginated(params = {}) {
    return apiClient.get("/alerts", { params }).then((r) => r.data);
  },

  // Workaround estable de alertas. Devuelve Page<AlertDTO> de Spring
  // (el índice de página es `number`; el campo de fecha es `triggeredAt`).
  // No acepta `severity` (se filtra en cliente).
  listAlerts({ zoneId, from, to, page = 0, size = 20 } = {}) {
    const params = { from, to, page, size, sort: "triggeredAt,desc" };
    if (zoneId) params.zoneId = zoneId;
    return apiClient.get("/compliance/alerts", { params }).then((r) => r.data);
  },
};