import { useEffect, useMemo, useState } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts";
import { analyticsApi } from "../api/analyticsApi";
import { Card } from "@/ui/primitives";

const DAY_LABELS = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];
const TOOLTIP_STYLE = {
  borderRadius: 10,
  border: "1px solid rgba(19,105,74,0.15)",
  fontFamily: "Manrope, sans-serif",
  fontSize: 12,
};

const currentYear = new Date().getFullYear();
const YEARS = Array.from({ length: 5 }, (_, i) => currentYear - i);

// Normaliza un punto del backend tolerando variantes de naming.
function normalizePoint(p) {
  const dow = p.weekday ?? p.dayOfWeek ?? p.dow ?? p.day;
  const avg = p.avgDb ?? p.laeqDb ?? p.avgLaeq ?? p.avg ?? p.value;
  const count = p.sampleCount ?? p.count ?? p.samples ?? p.n ?? 0;
  return { dow: Number(dow), avg: avg == null ? null : Number(avg), count: Number(count) };
}

export default function WeekdayChart({ zones = [] }) {
  const [zoneId, setZoneId] = useState("");
  const [year, setYear] = useState(currentYear);
  const [fromWeek, setFromWeek] = useState(1);
  const [toWeek, setToWeek] = useState(53);
  const [raw, setRaw] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    const params = { year, isoWeekFrom: fromWeek, isoWeekTo: toWeek };
    if (zoneId) params.zoneId = zoneId;
    analyticsApi
      .weekday(params)
      .then((res) => {
        if (cancelled) return;
        const list = Array.isArray(res) ? res : res?.content ?? res?.data ?? [];
        setRaw(list.map(normalizePoint));
      })
      .catch((err) => !cancelled && setError(err))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [zoneId, year, fromWeek, toWeek]);

  // Garantiza los 7 días en orden Dom..Sáb.
  const chartData = useMemo(() => {
    const byDow = new Map(raw.map((p) => [p.dow, p]));
    return DAY_LABELS.map((label, i) => {
      const p = byDow.get(i);
      return { day: label, avgDb: p?.avg ?? null, sampleCount: p?.count ?? 0 };
    });
  }, [raw]);

  const hasData = chartData.some((d) => d.avgDb != null);

  return (
    <Card>
      <div className="flex items-start justify-between gap-4 flex-wrap mb-3">
        <h3 className="font-display text-sm font-semibold text-ink">
          Comparativo por día de la semana — promedio dB(A)
        </h3>
        <div className="flex flex-wrap gap-2 items-center">
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
          <select
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="border border-slate-300 rounded px-2 py-1 text-xs"
          >
            {YEARS.map((y) => <option key={y} value={y}>{y}</option>)}
          </select>
          <label className="text-xs text-slate-500 flex items-center gap-1">
            Semanas
            <input
              type="number" min={1} max={53} value={fromWeek}
              onChange={(e) => setFromWeek(Math.min(53, Math.max(1, Number(e.target.value) || 1)))}
              className="w-14 border border-slate-300 rounded px-1.5 py-1 text-xs"
            />
            –
            <input
              type="number" min={1} max={53} value={toWeek}
              onChange={(e) => setToWeek(Math.min(53, Math.max(1, Number(e.target.value) || 53)))}
              className="w-14 border border-slate-300 rounded px-1.5 py-1 text-xs"
            />
          </label>
        </div>
      </div>

      {loading ? (
        <div className="h-72 bg-slate-100 animate-pulse rounded-xl2" />
      ) : error ? (
        <div className="h-72 flex items-center justify-center text-danger text-sm">
          Error al cargar el comparativo semanal
        </div>
      ) : !hasData ? (
        <div className="h-72 flex items-center justify-center text-muted text-sm">
          Sin datos para los filtros seleccionados
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(19,105,74,0.10)" />
            <XAxis dataKey="day" tick={{ fontSize: 12, fill: "#41524a" }} />
            <YAxis
              label={{ value: "dB(A)", angle: -90, position: "insideLeft", fill: "#41524a" }}
              tick={{ fill: "#41524a" }}
              domain={["dataMin - 2", "dataMax + 2"]}
            />
            <Tooltip
              contentStyle={TOOLTIP_STYLE}
              formatter={(v, _n, item) => [
                v == null ? "—" : `${Number(v).toFixed(1)} dB`,
                `Promedio (${item.payload.sampleCount} muestras)`,
              ]}
            />
            <Bar dataKey="avgDb" radius={[6, 6, 0, 0]}>
              {chartData.map((d, i) => (
                <Cell key={i} fill={i === 0 || i === 6 ? "#F0A02A" : "#13694A"} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
