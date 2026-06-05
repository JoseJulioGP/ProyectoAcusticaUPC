import { useEffect, useMemo, useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine,
} from "recharts";
import { analyticsApi } from "../api/analyticsApi";
import { Card } from "@/ui/primitives";

const TOOLTIP_STYLE = {
  borderRadius: 10,
  border: "1px solid rgba(19,105,74,0.15)",
  fontFamily: "Manrope, sans-serif",
  fontSize: 12,
};

const todayIso = () => new Date().toISOString().slice(0, 10);
const byBucket = (a, b) => String(a.bucket).localeCompare(String(b.bucket));
const num = (v) => (v == null ? null : Number(v));
// Backend: DailyAvgDTO.day (OffsetDateTime) + avgDb. Resto = tolerancia defensiva.
const bucketOf = (p) => p.day ?? p.bucket ?? p.date ?? p.time ?? p.timestamp;
const valOf = (p) => p.avgDb ?? p.laeqDb ?? p.value ?? p.db;

// Backend devuelve BeforeAfterDTO { before:[DailyAvgDTO], after:[DailyAvgDTO] }.
// Se tolera además array plano [{bucket,before,after}] | { points:[] }.
function buildSeries(res) {
  if (!res) return { rows: [], pivotBucket: null };

  if (res.before || res.after) {
    const map = new Map();
    const put = (arr, key) =>
      (arr ?? []).forEach((p) => {
        const bucket = bucketOf(p);
        const row = map.get(bucket) ?? { bucket };
        row[key] = num(valOf(p));
        map.set(bucket, row);
      });
    put(res.before, "before");
    put(res.after, "after");
    // El pivote visual = primer bucket del tramo "after" (boundary real en los datos).
    const pivotBucket = res.after?.length ? bucketOf(res.after[0]) : null;
    return { rows: [...map.values()].sort(byBucket), pivotBucket };
  }

  const arr = Array.isArray(res) ? res : res.points ?? res.content ?? [];
  const rows = arr
    .map((p) => ({ bucket: bucketOf(p), before: num(p.before), after: num(p.after) }))
    .sort(byBucket);
  const pivotBucket = rows.find((r) => r.after != null)?.bucket ?? null;
  return { rows, pivotBucket };
}

export default function BeforeAfterChart({ zones = [] }) {
  const [zoneId, setZoneId] = useState(zones[0]?.id ?? "");
  const [pivot, setPivot] = useState(todayIso());
  const [res, setRes] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!zoneId) return;
    let cancelled = false;
    setLoading(true);
    setError(null);
    // El backend espera OffsetDateTime; el date-picker da yyyy-mm-dd → añadimos hora/offset Colombia.
    analyticsApi
      .beforeAfter({ zoneId, pivot: `${pivot}T00:00:00-05:00` })
      .then((r) => !cancelled && setRes(r))
      .catch((err) => !cancelled && setError(err))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [zoneId, pivot]);

  const { rows, pivotBucket } = useMemo(() => buildSeries(res), [res]);
  const hasData = rows.some((r) => r.before != null || r.after != null);

  return (
    <Card>
      <div className="flex items-start justify-between gap-4 flex-wrap mb-3">
        <h3 className="font-display text-sm font-semibold text-ink">
          Antes / Después — impacto de una acción correctiva
        </h3>
        <div className="flex flex-wrap gap-2 items-center">
          <select
            value={zoneId}
            onChange={(e) => setZoneId(e.target.value)}
            className="border border-slate-300 rounded px-2 py-1 text-xs"
          >
            <option value="">Selecciona zona…</option>
            {zones.map((z) => (
              <option key={z.id} value={z.id}>{z.name}</option>
            ))}
          </select>
          <label className="text-xs text-slate-500 flex items-center gap-1">
            Pivote
            <input
              type="date"
              value={pivot}
              onChange={(e) => setPivot(e.target.value)}
              className="border border-slate-300 rounded px-1.5 py-1 text-xs"
            />
          </label>
        </div>
      </div>

      {!zoneId ? (
        <div className="h-72 flex items-center justify-center text-muted text-sm">
          Selecciona una zona para comparar antes/después.
        </div>
      ) : loading ? (
        <div className="h-72 bg-slate-100 animate-pulse rounded-xl2" />
      ) : error ? (
        <div className="h-72 flex items-center justify-center text-danger text-sm">
          Error al cargar la comparación
        </div>
      ) : !hasData ? (
        <div className="h-72 flex items-center justify-center text-muted text-sm">
          Sin datos alrededor de la fecha pivote
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={rows}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(19,105,74,0.10)" />
            <XAxis
              dataKey="bucket"
              tick={{ fontSize: 11, fill: "#41524a" }}
              tickFormatter={(v) => new Date(v).toLocaleDateString("es-CO", { dateStyle: "short" })}
            />
            <YAxis
              label={{ value: "dB(A)", angle: -90, position: "insideLeft", fill: "#41524a" }}
              tick={{ fill: "#41524a" }}
              domain={["dataMin - 2", "dataMax + 2"]}
            />
            <Tooltip
              contentStyle={TOOLTIP_STYLE}
              labelFormatter={(v) => new Date(v).toLocaleString("es-CO")}
              formatter={(v, n) => [v == null ? "—" : `${Number(v).toFixed(1)} dB`, n === "before" ? "Antes" : "Después"]}
            />
            <Legend formatter={(v) => (v === "before" ? "Antes" : "Después")} />
            {pivotBucket && (
              <ReferenceLine
                x={pivotBucket}
                stroke="#cf4f2c"
                strokeDasharray="4 4"
                label={{ value: "Pivote", fill: "#cf4f2c", fontSize: 11, position: "top" }}
              />
            )}
            <Line type="monotone" dataKey="before" stroke="#9ec3dd" strokeWidth={2} dot={false} connectNulls />
            <Line type="monotone" dataKey="after" stroke="#13694A" strokeWidth={2.5} dot={false} connectNulls />
          </LineChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
