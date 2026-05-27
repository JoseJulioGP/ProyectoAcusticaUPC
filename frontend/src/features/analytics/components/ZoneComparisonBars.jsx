import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine,
} from "recharts";
import { useComparison } from "../hooks/useComparison";

export default function ZoneComparisonBars() {
  const { data, loading, error } = useComparison();

  if (loading) return <div className="h-72 bg-slate-100 animate-pulse rounded"></div>;
  if (error) return <div className="text-red-600">Error al cargar comparativa</div>;
  if (!data || data.length === 0)
    return (
      <div className="h-72 flex items-center justify-center text-slate-500">
        Sin datos para comparar zonas
      </div>
    );

  return (
    <div className="bg-white rounded-lg border border-slate-200 p-4">
      <h3 className="text-sm font-semibold text-slate-700 mb-2">
        Comparativa por zona — LAeq diurno vs nocturno
      </h3>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="zoneName" tick={{ fontSize: 11 }} />
          <YAxis label={{ value: "dB(A)", angle: -90, position: "insideLeft" }} />
          <Tooltip formatter={(v) => (v == null ? "—" : `${v} dB`)} />
          <Legend />
          <Bar dataKey="laeqDiurnoDb" name="Diurno" fill="#f59e0b" />
          <Bar dataKey="laeqNocturnoDb" name="Nocturno" fill="#3b82f6" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}