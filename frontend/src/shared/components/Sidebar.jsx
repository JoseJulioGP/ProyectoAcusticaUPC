import { NavLink } from 'react-router-dom';
import { LayoutDashboard, MapPin, Upload, ShieldCheck, Users } from 'lucide-react';
import { useRole } from '../../features/auth/hooks/useRole';
import { Rombo } from '@/ui/Rombo';

const NAV = [
  { to: '/dashboard',   label: 'Dashboard',    icon: LayoutDashboard, allow: ['ADMIN', 'ANALYST', 'VIEWER'] },
  { to: '/zones',       label: 'Zonas',        icon: MapPin,          allow: ['ADMIN', 'ANALYST', 'VIEWER'], disabled: true },
  { to: '/ingest',      label: 'Ingesta',      icon: Upload,          allow: ['ADMIN'] },
  { to: '/compliance',  label: 'Cumplimiento', icon: ShieldCheck,     allow: ['ADMIN', 'ANALYST'] },
  { to: '/admin/users', label: 'Usuarios',     icon: Users,           allow: ['ADMIN'] },
];

export default function Sidebar() {
  const { role } = useRole();

  return (
    <aside
      className="w-60 flex flex-col"
      style={{
        background:
          'radial-gradient(120% 80% at 20% 0%, #0f5840 0%, #0c4836 45%, #0a3327 100%)',
      }}
    >
      <div className="flex items-center gap-2.5 px-5 py-5 border-b border-white/10">
        <Rombo size={26} gap="#0c4836" />
        <span className="font-display font-bold text-lg text-white">AcústicaUPC</span>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {NAV.filter(({ allow }) => role && allow.includes(role)).map(
          ({ to, label, icon: Icon, disabled }) =>
            disabled ? (
              <div
                key={to}
                className="flex items-center gap-3 px-3 py-2 rounded-lg text-white/40 cursor-not-allowed"
                title="Disponible en próximos sprints"
              >
                <Icon size={18} />
                <span className="text-sm font-body">{label}</span>
                <span className="ml-auto text-[10px] font-bold tracking-wide bg-white/10 px-1.5 py-0.5 rounded-full text-white/55">
                  próx.
                </span>
              </div>
            ) : (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-body transition-colors ${
                    isActive
                      ? 'bg-white/10 text-white font-semibold'
                      : 'text-white/75 hover:bg-white/10 hover:text-white'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    <Icon
                      size={18}
                      className={isActive ? 'text-rombo-verde' : ''}
                    />
                    <span>{label}</span>
                  </>
                )}
              </NavLink>
            )
        )}
      </nav>

      <div className="px-5 py-3 text-xs font-body text-white/40 border-t border-white/10">
        AcústicaUPC · v1.0.0 · Universidad Popular del Cesar · 2026
      </div>
    </aside>
  );
}
