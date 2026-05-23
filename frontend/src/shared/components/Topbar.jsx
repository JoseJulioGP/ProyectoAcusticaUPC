import { LogOut, User } from 'lucide-react';
import { useAuth } from '../../features/auth/hooks/useAuth';

export default function Topbar() {
  const { user, logout } = useAuth();

  return (
    <header className="h-14 bg-white border-b border-slate-200 flex items-center justify-between px-6">
      <h2 className="text-slate-700 font-medium">Panel de monitoreo acústico</h2>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 text-sm text-slate-600">
          <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center">
            <User size={16} className="text-indigo-600" />
          </div>
          <div className="flex flex-col leading-tight">
            <span className="font-medium text-slate-800">{user?.fullName}</span>
            <span className="text-xs text-slate-500">{user?.role}</span>
          </div>
        </div>

        <button
          onClick={logout}
          className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-slate-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
        >
          <LogOut size={16} />
          Salir
        </button>
      </div>
    </header>
  );
}
