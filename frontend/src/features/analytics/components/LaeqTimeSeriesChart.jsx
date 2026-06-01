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
} from 'recharts';
import { useTimeSeries } from '../hooks/useTimeSeries';
import { Card } from '@/ui/primitives';

const BRAND_COLORS = ['#2E93C9', '#8FBE3D', '#F0A02A', '#7A4E22', '#13694A'];
const TOOLTIP_STYLE = {
  borderRadius: 10,
  border: '1px solid rgba(19,105,74,0.15)',
  fontFamily: 'Manrope, sans-serif',
  fontSize: 12,
};

export default function LaeqTimeSeriesChart({ standardDb = 65 }) {
  const { data, loading, error } = useTimeSeries({ granularity: 'HOUR' });

  if (loading)
    return <div className="h-72 bg-slate-100 animate-pulse rounded-xl2"></div>;
  if (error)
    return <div className="text-danger text-sm">Error al cargar la serie</div>;
  if (!data || data.length === 0)
    return (
      <div className="h-72 flex items-center justify-center text-muted text-sm">
        Sin datos en el rango seleccionado
      </div>
    );

  const buckets = [...new Set(data.map((p) => p.bucket))].sort();
  const zones   = [...new Set(data.map((p) => p.zoneName))];

  const chartData = buckets.map((b) => {
    const row = { bucket: b };
    zones.forEach((zname) => {
      const point = data.find((p) => p.bucket === b && p.zoneName === zname);
      row[zname] = point ? point.laeqDb : null;
    });
    return row;
  });

  return (
    <Card>
      <h3 className="font-display text-sm font-semibold text-ink mb-3">
        LAeq por hora — Línea horizontal en estándar ({standardDb} dB)
      </h3>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(19,105,74,0.10)" />
          <XAxis
            dataKey="bucket"
            tick={{ fontSize: 11, fill: '#41524a' }}
            tickFormatter={(v) =>
              new Date(v).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' })
            }
          />
          <YAxis
            label={{ value: 'dB(A)', angle: -90, position: 'insideLeft', fill: '#41524a' }}
            tick={{ fill: '#41524a' }}
          />
          <Tooltip
            contentStyle={TOOLTIP_STYLE}
            labelFormatter={(v) => new Date(v).toLocaleString('es-CO')}
            formatter={(v) => (v == null ? '—' : `${v} dB`)}
          />
          <Legend />
          <ReferenceLine
            y={standardDb}
            stroke="#cf4f2c"
            strokeDasharray="4 4"
            label={{ value: 'Estándar', fill: '#cf4f2c', fontSize: 11 }}
          />
          {zones.map((z, i) => (
            <Line
              key={z}
              type="monotone"
              dataKey={z}
              stroke={BRAND_COLORS[i % BRAND_COLORS.length]}
              dot={false}
              connectNulls
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </Card>
  );
}
