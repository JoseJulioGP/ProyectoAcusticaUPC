import { Card } from '@/ui/primitives';

const ACCENT = {
  slate: '#13694A',
  blue:  '#2E93C9',
  green: '#8FBE3D',
  amber: '#F0A02A',
  red:   '#cf4f2c',
};

export default function KpiCard({ label, value, sub, accent = 'slate', loading = false }) {
  return (
    <Card accent={ACCENT[accent] ?? ACCENT.slate} lift>
      <p className="font-body text-xs uppercase tracking-wide text-muted">{label}</p>
      {loading ? (
        <div className="h-8 w-24 bg-slate-100 animate-pulse mt-2 rounded"></div>
      ) : (
        <p className="font-display text-3xl font-semibold text-ink mt-1">{value}</p>
      )}
      {sub && <p className="font-body text-xs text-ink2 mt-2">{sub}</p>}
    </Card>
  );
}
