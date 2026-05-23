import { Activity, TrendingUp, AlertTriangle, MapPin } from 'lucide-react';
import KpiCardSkeleton from '../components/KpiCardSkeleton';

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Dashboard</h1>
        <p className="text-slate-500 mt-1">
          Visión general del ruido ambiental en el campus. Los KPIs reales se conectarán
          en el Sprint 4.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCardSkeleton
          title="Promedio dB (hoy)"
          hint="Pendiente — requiere ingesta (Sprint 2)"
          icon={Activity}
          accent="indigo"
        />
        <KpiCardSkeleton
          title="Pico máximo"
          hint="Pendiente — requiere ingesta (Sprint 2)"
          icon={TrendingUp}
          accent="emerald"
        />
        <KpiCardSkeleton
          title="Zonas en alerta"
          hint="Pendiente — requiere cumplimiento (Sprint 3)"
          icon={AlertTriangle}
          accent="amber"
        />
        <KpiCardSkeleton
          title="Zonas monitoreadas"
          hint="Pendiente — requiere conexión zonas (Sprint 4)"
          icon={MapPin}
          accent="sky"
        />
      </div>

      <div className="bg-white border border-slate-200 rounded-xl p-8 text-center text-slate-400">
        <p className="text-sm">📊 Gráficas de línea temporal aquí (Sprint 4)</p>
      </div>
    </div>
  );
}