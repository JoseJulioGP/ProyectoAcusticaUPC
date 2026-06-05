import { useMemo, useState } from "react";
import { useTimeSeries } from "../hooks/useTimeSeries";
import { Card } from "@/ui/primitives";

/**
 * Resumen por franja horaria (00:00–23:59): promedio de ruido y nº de registros,
 * para la zona seleccionada (o todas). Agrupa la serie horaria por hora del día y
 * combina el LAeq por energía ponderada por muestras:
 *   LAeq = 10·log10( Σ(n·10^(LAeq_i/10)) / Σn )
 */
function buildHourly(points) {
  const acc = Array.from({ length: 24 }, () => ({ energy: 0, count: 0 }));
  for (const p of points) {
    const n = p.sampleCount ?? 0;
    if (n <= 0 || p.laeqDb == null) continue;
    const hour = new Date(p.bucket).getHours();
    acc[hour].energy += n * Math.pow(10, p.laeqDb / 10);
    acc[hour].count += n;
  }
  return acc.map((a, hour) => ({
    hour,
    avgDb: a.count > 0 ? 10 * Math.log10(a.energy / a.count) : null,
    count: a.count,
  }));
}

export default function HourlySummary({ zones = [] }) {
  const [zoneId, setZoneId] = useState("");
  const { data, loading, error } = useTimeSeries({ granularity: "HOUR", zoneId });

  const rows = useMemo(() => buildHourly(data ?? []).filter((r) => r.count > 0), [data]);
  const totalCount = rows.reduce((s, r) => s + r.count, 0);

  return (
    <Card className="overflow-x-auto">
      <div className="flex items-start justify-between gap-4 flex-wrap mb-3">
        <h3 className="font-display text-sm font-semibold text-ink">
          Resumen por hora — promedio de ruido y registros
        </h3>
        <select
          value={zoneId}
          onChange={(e) => setZoneId(e.target.value)}
          className="border border-slate-300 rounded px-2 py-1 text-xs"
        >
          <option value="">Todas las zonas</option>
          {zones.map((z) => (
            <option key={z.id} value={z.id}>{z.name}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="h-40 bg-slate-100 animate-pulse rounded-xl2" />
      ) : error ? (
        <div className="h-32 flex items-center justify-center text-danger text-sm">
          Error al cargar el resumen
        </div>
      ) : rows.length === 0 ? (
        <div className="h-32 flex items-center justify-center text-muted text-sm">
          Sin registros para la zona / rango seleccionado
        </div>
      ) : (
        <table className="min-w-full text-sm font-body">
          <thead>
            <tr className="text-left text-xs uppercase text-muted border-b border-petroleo/10">
              <th className="py-2 pr-4 font-semibold">Franja horaria</th>
              <th className="py-2 pr-4 font-semibold text-right">Promedio (dB)</th>
              <th className="py-2 font-semibold text-right">Registros</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.hour} className="acu-trow border-b border-petroleo/5 text-ink">
                <td className="py-1.5 pr-4 tabular-nums">
                  {String(r.hour).padStart(2, "0")}:00 – {String(r.hour).padStart(2, "0")}:59
                </td>
                <td className="py-1.5 pr-4 text-right tabular-nums font-mono">
                  {r.avgDb.toFixed(1)}
                </td>
                <td className="py-1.5 text-right tabular-nums">
                  {r.count.toLocaleString("es-CO")}
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr className="font-semibold text-ink">
              <td className="py-2 pr-4">Total</td>
              <td className="py-2 pr-4 text-right text-muted">—</td>
              <td className="py-2 text-right tabular-nums">{totalCount.toLocaleString("es-CO")}</td>
            </tr>
          </tfoot>
        </table>
      )}
    </Card>
  );
}
