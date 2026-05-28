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
import { useTimeSeries } from "../hooks/useTimeSeries";

const COLORS = ["#0ea5e9", "#f97316", "#10b981", "#a855f7", "#ef4444"];

export default function LaeqTimeSeriesChart({ standardDb = 65 }) {
  const { data, loading, error } = useTimeSeries({ granularity: "HOUR" });

  if (loading)
    return <div className="h-72 bg-slate-100 animate-pulse rounded"></div>;
  if (error) return <div className="text-red-600">Error al cargar la serie</div>;
  if (!data || data.length === 0)
    return (
      <div className="h-72 flex items-center justify-center text-slate-500">
        Sin datos en el rango seleccionado
      </div>
    );

  // Reorganiza: cada zona es una línea, ejex = bucket
  const buckets = [...new Set(data.map((p) => p.bucket))].sort();
  const zones = [...new Set(data.map((p) => p.zoneName))];

  const chartData = buckets.map((b) => {
    const row = { bucket: b };
    zones.forEach((zname) => {
      const point = data.find((p) => p.bucket === b && p.zoneName === zname);
      row[zname] = point ? point.laeqDb : null;
    });
    return row;
  });

  return (
    <div className="bg-white rounded-lg border border-slate-200 p-4">
      <h3 className="text-sm font-semibold text-slate-700 mb-2">
        LAeq por hora — Línea horizontal en estándar ({standardDb} dB)
      </h3>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis
            dataKey="bucket"
            tickFormatter={(v) => new Date(v).toLocaleString("es-CO", { dateStyle: "short", timeStyle: "short" })}
          />
          <YAxis label={{ value: "dB(A)", angle: -90, position: "insideLeft" }} />
          <Tooltip
            labelFormatter={(v) => new Date(v).toLocaleString("es-CO")}
            formatter={(v) => (v == null ? "—" : `${v} dB`)}
          />
          <Legend />
          <ReferenceLine
            y={standardDb}
            stroke="#dc2626"
            strokeDasharray="4 4"
            label={{ value: "Estándar", fill: "#dc2626", fontSize: 11 }}
          />
          {zones.map((z, i) => (
            <Line
              key={z}
              type="monotone"
              dataKey={z}
              stroke={COLORS[i % COLORS.length]}
              dot={false}
              connectNulls
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}