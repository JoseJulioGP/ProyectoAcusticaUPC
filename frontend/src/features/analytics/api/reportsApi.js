import apiClient from "../../../shared/api/apiClient";

/** Convierte ISO ("2025-06-01T06:28:45.718Z") o Date a "yyyy-MM-dd". */
function toIsoDate(v) {
  if (!v) return v;
  if (v instanceof Date) return v.toISOString().slice(0, 10);
  if (typeof v === "string" && v.length >= 10) return v.slice(0, 10);
  return v;
}

export async function downloadCompliancePdf({ from, to, zoneId }) {
  const params = new URLSearchParams();
  if (from) params.set("from", toIsoDate(from));
  if (to) params.set("to", toIsoDate(to));
  if (zoneId) params.set("zoneId", zoneId);

  const res = await apiClient.get(`/reports/compliance/pdf?${params.toString()}`, {
    responseType: "blob",
  });

  const cd = res.headers["content-disposition"] ?? "";
  const match = cd.match(/filename="?([^"]+)"?/);
  const filename = match ? match[1] : "reporte-cumplimiento.pdf";
  const url = window.URL.createObjectURL(new Blob([res.data], { type: "application/pdf" }));
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

const XLSX_MIME =
  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

/** Exporta el dashboard a Excel (RF-09). Dispara la descarga en el navegador. */
export async function downloadDashboardXlsx({ from, to, zoneId } = {}) {
  const params = new URLSearchParams();
  if (from) params.set("from", toIsoDate(from));
  if (to) params.set("to", toIsoDate(to));
  if (zoneId) params.set("zoneId", zoneId);

  const res = await apiClient.get(`/reports/dashboard.xlsx?${params.toString()}`, {
    responseType: "blob",
  });

  const cd = res.headers["content-disposition"] ?? "";
  const match = cd.match(/filename="?([^"]+)"?/);
  const filename = match ? match[1] : "dashboard.xlsx";
  const url = window.URL.createObjectURL(new Blob([res.data], { type: XLSX_MIME }));
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}