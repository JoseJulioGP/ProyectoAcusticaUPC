import { NavLink } from 'react-router-dom';
import { LayoutDashboard, MapPin, Upload, ShieldCheck, Volume2 } from 'lucide-react';

const NAV = [
  { to: '/dashboard',  label: 'Dashboard',    icon: LayoutDashboard },
  { to: '/zones',      label: 'Zonas',        icon: MapPin,        disabled: true },
  { to: '/ingest',     label: 'Ingesta',      icon: Upload },
  { to: '/compliance', label: 'Cumplimiento', icon: ShieldCheck },
];

export default function Sidebar() {
  return (
    <aside className="w-60 bg-slate-900 text-slate-100 flex flex-col">
      <div className="flex items-center gap-2 px-5 py-5 border-b border-slate-800">
        <Volume2 size={22} className="text-indigo-400" />
        <span className="font-bold text-lg">AcústicaUPC</span>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {NAV.map(({ to, label, icon: Icon, disabled }) =>
          disabled ? (
            <div
              key={to}
              className="flex items-center gap-3 px-3 py-2 rounded-lg text-slate-500 cursor-not-allowed"
              title="Disponible en próximos sprints"
            >
              <Icon size={18} />
              <span className="text-sm">{label}</span>
              <span className="ml-auto text-[10px] bg-slate-800 px-1.5 py-0.5 rounded">próx.</span>
            </div>
          ) : (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${
                  isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-800'
                }`
              }
            >
              <Icon size={18} />
              <span>{label}</span>
            </NavLink>
          )
        )}
      </nav>

      <div className="px-5 py-3 text-xs text-slate-500 border-t border-slate-800">
        v0.3.0 — Sprint 3
      </div>
    </aside>
  );
}