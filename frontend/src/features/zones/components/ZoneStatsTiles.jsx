import { Link } from "react-router-dom";

/**
 * Tarjetas de estado por zona (clic → detalle). Es el contenido que antes vivía
 * en el panel "Zonas — clic para detalle" del dashboard.
 * `stats` viene de /analytics/zones/stats; `range` son los from/to usados.
 */
export default function ZoneStatsTiles({ stats, range }) {
  if (!stats || stats.length === 0) return null;
  const qs = `?from=${encodeURIComponent(range.from)}&to=${encodeURIComponent(range.to)}`;

  return (
    <div>
      <h2 className="text-lg font-semibold text-slate-800 mb-3">
        Estado de zonas — clic para detalle
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        {stats.map((z) => (
          <Link
            key={z.zoneId}
            to={`/dashboard/zones/${z.zoneId}${qs}`}
            className="acu-row block p-3 border border-slate-200 rounded-xl bg-white"
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
    </div>
  );
}
