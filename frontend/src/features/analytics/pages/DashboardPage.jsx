import { DashboardFilterProvider, useDashboardFilter } from "../context/DashboardFilterContext";
import GlobalFilters from "../components/GlobalFilters";
import KpiCard from "../components/KpiCard";
import ProgressBar from "../components/ProgressBar";
import LaeqTimeSeriesChart from "../components/LaeqTimeSeriesChart";
import ZoneComparisonBars from "../components/ZoneComparisonBars";
import HeatmapGrid from "../components/HeatmapGrid";
import WeekdayChart from "../components/WeekdayChart";
import BeforeAfterChart from "../components/BeforeAfterChart";
import HourlySummary from "../components/HourlySummary";
import { useKpis } from "../hooks/useKpis";
import { Link } from "react-router-dom";
import { useState, useEffect } from "react";
import apiClient from "../../../shared/api/apiClient";
import { downloadCompliancePdf } from "../api/reportsApi";
import ExportExcelButton from "../components/ExportExcelButton";

function ExportPdfButton() {
  const { from, to, zoneId } = useDashboardFilter();
  const [busy, setBusy] = useState(false);
  const onClick = async () => {
    setBusy(true);
    try { await downloadCompliancePdf({ from, to, zoneId }); }
    catch { alert("No se pudo generar el PDF."); }
    finally { setBusy(false); }
  };
  return (
    <button onClick={onClick} disabled={busy}
            className="rounded bg-emerald-600 px-4 py-2 text-white hover:bg-emerald-700 disabled:opacity-50">
      {busy ? "Generando…" : "Exportar PDF"}
    </button>
  );
}

function DashboardContent() {
  const { period, zoneId } = useDashboardFilter();
  const { data: kpis, loading: kpisLoading } = useKpis();

  const [zones, setZones] = useState([]);
  useEffect(() => {
    apiClient
      .get("/zones")
      .then((r) => setZones(Array.isArray(r.data) ? r.data : r.data.content ?? []))
      .catch(() => setZones([]));
  }, []);

  const standardDb = period === "NOCTURNO" ? 55 : 65;
  // "Zonas activas" = zonas registradas activas (no depende del rango de fechas).
  const activeZonesCount = zones.filter((z) => z.active !== false).length;

  return (
    <div className="acu-stagger max-w-7xl mx-auto">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-4">
        <h1 className="text-xl sm:text-2xl font-bold text-slate-800">Dashboard de Acústica UPC</h1>
        <div className="flex flex-wrap items-center gap-2">
          <ExportExcelButton />
          <ExportPdfButton />
        </div>
      </div>

      <GlobalFilters />

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <Link to="/ingest" className="block">
          <KpiCard
            label="Mediciones"
            value={kpis?.totalMeasurements}
            sub="En el rango seleccionado"
            accent="petroleo"
            loading={kpisLoading}
          />
        </Link>
        <Link to="/admin/zones" className="block">
          <KpiCard
            label="Zonas activas"
            value={activeZonesCount}
            sub="Zonas registradas activas"
            accent="blue"
            loading={kpisLoading && zones.length === 0}
          />
        </Link>
        <Link to={`/alerts${zoneId ? `?zoneId=${encodeURIComponent(zoneId)}` : ""}`} className="block">
          <KpiCard
            label="Alertas"
            value={kpis?.alertsActive}
            accent="amber"
            loading={kpisLoading}
            footer={
              kpisLoading || !kpis ? null : (
                <div className="flex flex-wrap gap-x-3 gap-y-1 font-body text-xs font-semibold">
                  <span className="whitespace-nowrap text-rose-600">● {kpis.alertsBySeverity.CRITICA ?? 0} críticas</span>
                  <span className="whitespace-nowrap text-[#9a6212]">● {kpis.alertsBySeverity.MODERADA ?? 0} moderadas</span>
                  <span className="whitespace-nowrap text-[#C98A12]">● {kpis.alertsBySeverity.LEVE ?? 0} leves</span>
                </div>
              )
            }
          />
        </Link>
        <Link to={`/alerts${zoneId ? `?zoneId=${encodeURIComponent(zoneId)}` : ""}`} className="block">
          <KpiCard
            label="% Cumplimiento"
            value={kpis?.complianceRatePercent}
            suffix="%"
            accent="green"
            loading={kpisLoading}
            footer={
              kpisLoading || !kpis ? null : (
                <div>
                  <ProgressBar pct={kpis.complianceRatePercent} />
                  <span className="mt-1.5 block font-body text-[11px] text-muted">CUMPLE / total evaluados</span>
                </div>
              )
            }
          />
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
        <LaeqTimeSeriesChart standardDb={standardDb} />
        <ZoneComparisonBars />
      </div>

      <h2 className="text-lg font-semibold text-slate-800 mb-3">Tendencias</h2>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
        <WeekdayChart zones={zones} />
        <BeforeAfterChart zones={zones} />
      </div>

      <div className="mb-6">
        <HeatmapGrid />
      </div>

      <div className="mb-6">
        <HourlySummary zones={zones} />
      </div>
    </div>
  );
}

export default function DashboardPage() {
  return (
    <DashboardFilterProvider>
      <DashboardContent />
    </DashboardFilterProvider>
  );
}