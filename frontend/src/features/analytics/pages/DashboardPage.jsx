import { DashboardFilterProvider, useDashboardFilter } from "../context/DashboardFilterContext";
import GlobalFilters from "../components/GlobalFilters";
import KpiCard from "../components/KpiCard";
import LaeqTimeSeriesChart from "../components/LaeqTimeSeriesChart";
import ZoneComparisonBars from "../components/ZoneComparisonBars";
import HeatmapGrid from "../components/HeatmapGrid";
import AlertsBadgeList from "../components/AlertsBadgeList";
import { useKpis } from "../hooks/useKpis";
import { useZonesStats } from "../hooks/useZonesStats";
import { Link } from "react-router-dom";

function DashboardContent() {
  const { period } = useDashboardFilter();
  const { data: kpis, loading: kpisLoading } = useKpis();
  const { data: zonesStats, loading: zonesLoading } = useZonesStats();

  // El estándar a graficar depende del período seleccionado
  
  const standardDb = period === "NOCTURNO" ? 55 : 65;

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <h1 className="text-2xl font-bold text-slate-800 mb-4">Dashboard de Acústica UPC</h1>

      <GlobalFilters />

      {/* KPI cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCard
          label="Mediciones"
          value={kpisLoading ? "—" : kpis.totalMeasurements.toLocaleString("es-CO")}
          sub="En el rango seleccionado"
          loading={kpisLoading}
        />
        <KpiCard
          label="Zonas activas"
          value={kpisLoading ? "—" : kpis.zonesActive}
          sub="Con al menos una medición"
          accent="blue"
          loading={kpisLoading}
        />
        <KpiCard
          label="Alertas"
          value={kpisLoading ? "—" : kpis.alertsActive}
          sub={
            kpisLoading
              ? "—"
              : `${kpis.alertsBySeverity.CRITICA ?? 0} críticas · ${kpis.alertsBySeverity.MODERADA ?? 0} moderadas · ${kpis.alertsBySeverity.LEVE ?? 0} leves`
          }
          accent={kpis?.alertsActive > 0 ? "red" : "green"}
          loading={kpisLoading}
        />
        <KpiCard
          label="% Cumplimiento"
          value={kpisLoading ? "—" : `${kpis.complianceRatePercent}%`}
          sub="CUMPLE / total evaluados"
          accent={kpis?.complianceRatePercent >= 80 ? "green" : "amber"}
          loading={kpisLoading}
        />
      </div>

      {/* Línea temporal + comparativa */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
        <LaeqTimeSeriesChart standardDb={standardDb} />
        <ZoneComparisonBars />
      </div>

      {/* Heatmap */}
      <div className="mb-6">
        <HeatmapGrid />
      </div>

      {/* Lista de zonas con stats + drilldown */}
      <div className="bg-white rounded-lg border border-slate-200 p-4 mb-6">
        <h3 className="text-sm font-semibold text-slate-700 mb-3">Zonas — clic para detalle</h3>
        {zonesLoading ? (
          <div className="h-24 bg-slate-100 animate-pulse rounded"></div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {zonesStats.map((z) => (
              <Link
                key={z.zoneId}
                to={`/dashboard/zones/${z.zoneId}`}
                className="block p-3 border border-slate-200 rounded hover:bg-slate-50"
              >
                <div className="flex justify-between items-start">
                  <div>
                    <p className="font-medium text-slate-800">{z.zoneName}</p>
                    <p className="text-xs text-slate-500">
                      Sector {z.sector} — {z.subsector}
                    </p>
                  </div>
                  <span
                    className={`text-xs px-2 py-0.5 rounded ${
                      z.overallStatus === "CUMPLE"
                        ? "bg-green-100 text-green-700"
                        : "bg-red-100 text-red-700"
                    }`}
                  >
                    {z.overallStatus}
                  </span>
                </div>
                <div className="mt-2 text-xs text-slate-600 flex gap-3">
                  <span>Día: {z.laeqDiurnoDb ?? "—"} / {z.standardDayDb} dB</span>
                  <span>Noche: {z.laeqNocturnoDb ?? "—"} / {z.standardNightDb} dB</span>
                </div>
                <p className="text-xs text-slate-500 mt-1">
                  {z.measurements.toLocaleString("es-CO")} mediciones · {z.alertsCount} alertas
                </p>
              </Link>
            ))}
          </div>
        )}
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