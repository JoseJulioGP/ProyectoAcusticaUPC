import { useState } from "react";
import { FileSpreadsheet, Loader2 } from "lucide-react";
import { useDashboardFilter } from "../context/DashboardFilterContext";
import { downloadDashboardXlsx } from "../api/reportsApi";
import { useToast } from "../../../shared/ui/useToast";

/**
 * Botón "Exportar Excel" del dashboard. Visible para todos los roles (incluido
 * VIEWER) — cierra el bug del Sprint 6.
 */
export default function ExportExcelButton() {
  const { from, to, zoneId } = useDashboardFilter();
  const toast = useToast();
  const [busy, setBusy] = useState(false);

  const onClick = async () => {
    setBusy(true);
    try {
      await downloadDashboardXlsx({ from, to, zoneId });
      toast.success("Excel descargado.");
    } catch (err) {
      toast.error(err.response?.data?.message || "No se pudo generar el Excel.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <button
      onClick={onClick}
      disabled={busy}
      className="inline-flex items-center gap-2 rounded bg-petroleo px-4 py-2 text-white hover:bg-petroleo/90 disabled:opacity-50"
    >
      {busy ? <Loader2 size={16} className="animate-spin" /> : <FileSpreadsheet size={16} />}
      {busy ? "Generando…" : "Exportar Excel"}
    </button>
  );
}
