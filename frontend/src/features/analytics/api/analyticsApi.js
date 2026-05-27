import apiClient from "../../../shared/api/apiClient";

const BASE = "/analytics";

export const analyticsApi = {
  kpis: ({ from, to, zoneId, period }) =>
    apiClient.get(`${BASE}/kpis`, { params: { from, to, zoneId, period } }),

  zonesStats: ({ from, to }) =>
    apiClient.get(`${BASE}/zones/stats`, { params: { from, to } }),

  zoneDetail: (zoneId, { from, to }) =>
    apiClient.get(`${BASE}/zones/${zoneId}`, { params: { from, to } }),

  alertsSummary: ({ from, to }) =>
    apiClient.get(`${BASE}/alerts/summary`, { params: { from, to } }),

  timeseries: ({ from, to, zoneId, period, granularity = "HOUR" }) =>
    apiClient.get(`${BASE}/timeseries`, {
      params: { from, to, zoneId, period, granularity },
    }),

  heatmap: ({ from, to, period }) =>
    apiClient.get(`${BASE}/heatmap`, { params: { from, to, period } }),

  comparison: ({ from, to, zoneIds, period }) =>
    apiClient.get(`${BASE}/comparison`, {
      params: {
        from,
        to,
        zoneIds: zoneIds && zoneIds.length ? zoneIds.join(",") : undefined,
        period,
      },
    }),
};