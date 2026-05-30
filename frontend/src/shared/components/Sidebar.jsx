import { NavLink } from 'react-router-dom';
import { LayoutDashboard, MapPin, Upload, ShieldCheck, Volume2 } from 'lucide-react';
import { useRole } from '../../features/auth/hooks/useRole';
import RoleGate from "../../auth/components/RoleGate";

const NAV = [
  // Dashboard: visible para todos
  { to: '/dashboard',  label: 'Dashboard',    icon: LayoutDashboard, allow: ['ADMIN', 'ANALYST', 'VIEWER'] },
  // Zonas: deshabilitado, próximos sprints
  { to: '/zones',      label: 'Zonas',        icon: MapPin,          allow: ['ADMIN', 'ANALYST', 'VIEWER'], disabled: true },
  // Ingesta: solo ADMIN
  { to: '/ingest',     label: 'Ingesta',      icon: Upload,          allow: ['ADMIN'] },
  // Cumplimiento: ADMIN + ANALYST
  { to: '/compliance', label: 'Cumplimiento', icon: ShieldCheck,     allow: ['ADMIN', 'ANALYST'] },
  // Gestión de usuarios: solo ADMIN
  { to: '/admin/users', label: 'Usuarios', icon: Users, allow: ['ADMIN'] },

];

export default function Sidebar() {
  const { role } = useRole();

  return (
    <aside className="w-60 bg-slate-900 text-slate-100 flex flex-col">
      <div className="flex items-center gap-2 px-5 py-5 border-b border-slate-800">
        <Volume2 size={22} className="text-indigo-400" />
        <span className="font-bold text-lg">AcústicaUPC</span>
      </div>
      <nav className="flex-1 px-3 py-4 space-y-1">
        {NAV
          // Solo muestra los enlaces permitidos para el rol del usuario logueado
          .filter(({ allow }) => role && allow.includes(role))
          .map(({ to, label, icon: Icon, disabled }) =>
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
        AcústicaUPC · v1.0.0 · Universidad Popular del Cesar · 2026
      </div>
    </aside>
  );
}