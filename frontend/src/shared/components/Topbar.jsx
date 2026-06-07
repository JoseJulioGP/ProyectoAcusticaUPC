import { LogOut, User, Menu } from 'lucide-react';
import { useAuth } from '../../features/auth/hooks/useAuth';

export default function Topbar({ onMenuClick }) {
  const { user, logout } = useAuth();

  return (
    <header className="h-14 bg-paper/85 backdrop-blur-sm border-b border-petroleo/10 flex items-center justify-between gap-2 px-4 md:px-6 sticky top-0 z-10">
      <div className="flex items-center gap-2 md:gap-3 min-w-0">
        {/* Hamburguesa: abre el drawer (solo móvil) */}
        <button
          type="button"
          onClick={onMenuClick}
          aria-label="Abrir menú"
          className="shrink-0 text-ink2 hover:text-petroleo md:hidden"
        >
          <Menu size={22} />
        </button>
        <h2 className="font-display font-semibold text-ink truncate text-sm md:text-base">
          <span className="md:hidden">Monitoreo acústico</span>
          <span className="hidden md:inline">Panel de monitoreo acústico</span>
        </h2>
      </div>

      <div className="flex items-center gap-2 md:gap-4 shrink-0">
        <div className="flex items-center gap-2 text-sm">
          <div className="w-8 h-8 bg-petroleo/10 rounded-full flex items-center justify-center shrink-0">
            <User size={16} className="text-petroleo" />
          </div>
          <div className="hidden sm:flex flex-col leading-tight">
            <span className="font-display font-semibold text-ink text-sm">{user?.fullName}</span>
            <span className="font-body text-xs text-muted">{user?.role}</span>
          </div>
        </div>

        <button
          onClick={logout}
          aria-label="Salir"
          className="flex items-center gap-1.5 px-2 md:px-3 py-1.5 text-sm font-body text-ink2 hover:text-danger hover:bg-danger/10 rounded-lg transition-colors"
        >
          <LogOut size={16} />
          <span className="hidden sm:inline">Salir</span>
        </button>
      </div>
    </header>
  );
}
