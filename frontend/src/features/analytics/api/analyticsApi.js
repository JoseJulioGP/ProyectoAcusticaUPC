import apiClient from "../../../shared/api/apiClient";

export const analyticsApi = {
  kpis({ from, to, zoneId, period } = {}) {
    return apiClient
      .get("/analytics/kpis", { params: { from, to, zoneId, period } })
      .then((r) => r.data);
  },

  zonesStats({ from, to } = {}) {
    return apiClient
      .get("/analytics/zones/stats", { params: { from, to } })
      .then((r) => r.data);
  },

  zoneDetail(zoneId, { from, to } = {}) {
    return apiClient
      .get(`/analytics/zones/${zoneId}`, { params: { from, to } })
      .then((r) => r.data);
  },

  alertsSummary({ from, to } = {}) {
    return apiClient
      .get("/analytics/alerts/summary", { params: { from, to } })
      .then((r) => r.data);
  },

  timeseries({ from, to, zoneId, period, granularity = "HOUR" } = {}) {
    return apiClient
      .get("/analytics/timeseries", {
        params: { from, to, zoneId, period, granularity },
      })
      .then((r) => r.data);
  },

  heatmap({ from, to, period } = {}) {
    return apiClient
      .get("/analytics/heatmap", { params: { from, to, period } })
      .then((r) => r.data);
  },

  comparison({ from, to, zoneIds, period } = {}) {
    return apiClient
      .get("/analytics/comparison", {
        params: {
          from,
          to,
          zoneIds: zoneIds && zoneIds.length ? zoneIds.join(",") : undefined,
          period,
        },
      })
      .then((r) => r.data);
  },
};