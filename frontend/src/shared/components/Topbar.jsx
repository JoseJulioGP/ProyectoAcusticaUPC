import { LogOut, User } from 'lucide-react';
import { useAuth } from '../../features/auth/hooks/useAuth';

export default function Topbar() {
  const { user, logout } = useAuth();

  return (
    <header className="h-14 bg-paper/85 backdrop-blur-sm border-b border-petroleo/10 flex items-center justify-between px-6 sticky top-0 z-10">
      <h2 className="font-display font-semibold text-ink">Panel de monitoreo acústico</h2>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 text-sm">
          <div className="w-8 h-8 bg-petroleo/10 rounded-full flex items-center justify-center">
            <User size={16} className="text-petroleo" />
          </div>
          <div className="flex flex-col leading-tight">
            <span className="font-display font-semibold text-ink text-sm">{user?.fullName}</span>
            <span className="font-body text-xs text-muted">{user?.role}</span>
          </div>
        </div>

        <button
          onClick={logout}
          className="flex items-center gap-1.5 px-3 py-1.5 text-sm font-body text-ink2 hover:text-danger hover:bg-danger/10 rounded-lg transition-colors"
        >
          <LogOut size={16} />
          Salir
        </button>
      </div>
    </header>
  );
}
