import apiClient from "../../../shared/api/apiClient";

export async function downloadCompliancePdf({ from, to, zoneId }) {
  const params = new URLSearchParams();
  if (from) params.set("from", from);
  if (to) params.set("to", to);
  if (zoneId) params.set("zoneId", zoneId);

  const res = await apiClient.get(`/reports/compliance/pdf?${params.toString()}`, {
    responseType: "blob",
  });

  // Toma el filename del Content-Disposition (lo expone el CORS del backend).
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