import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useComparison } from '../hooks/useComparison';
import { Card } from '@/ui/primitives';

const TOOLTIP_STYLE = {
  borderRadius: 10,
  border: '1px solid rgba(19,105,74,0.15)',
  fontFamily: 'Manrope, sans-serif',
  fontSize: 12,
};

export default function ZoneComparisonBars() {
  const { data, loading, error } = useComparison();

  if (loading)
    return <div className="h-72 bg-slate-100 animate-pulse rounded-xl2"></div>;
  if (error)
    return <div className="text-danger text-sm">Error al cargar comparativa</div>;
  if (!data || data.length === 0)
    return (
      <div className="h-72 flex items-center justify-center text-muted text-sm">
        Sin datos para comparar zonas
      </div>
    );

  return (
    <Card>
      <h3 className="font-display text-sm font-semibold text-ink mb-3">
        Comparativa por zona — LAeq diurno vs nocturno
      </h3>
      <ResponsiveContainer width="100%" height={320}>
        <BarChart data={data} margin={{ top: 5, right: 10, left: 0, bottom: 30 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(19,105,74,0.10)" />
          <XAxis
            dataKey="zoneName"
            interval={0}
            angle={-22}
            textAnchor="end"
            height={72}
            tick={{ fontSize: 10, fill: '#41524a' }}
            tickFormatter={(v) => (v && v.length > 16 ? v.slice(0, 15) + '…' : v)}
          />
          <YAxis
            label={{ value: 'dB(A)', angle: -90, position: 'insideLeft', fill: '#41524a' }}
            tick={{ fill: '#41524a' }}
          />
          <Tooltip
            contentStyle={TOOLTIP_STYLE}
            formatter={(v) => (v == null ? '—' : `${v} dB`)}
          />
          <Legend />
          <Bar dataKey="laeqDiurnoDb"   name="Diurno"   fill="#F0A02A" radius={[4, 4, 0, 0]} />
          <Bar dataKey="laeqNocturnoDb" name="Nocturno" fill="#2E93C9" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </Card>
  );
}
