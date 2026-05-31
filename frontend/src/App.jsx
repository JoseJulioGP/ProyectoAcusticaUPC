import { useState } from 'react';
import { useAuth } from '@/lib/auth';
import { Login } from '@/ui/Login';
import { Shell } from '@/ui/Shell';

export default function App() {
  const { token } = useAuth();
  const [view, setView] = useState('dashboard');

  if (!token) return <Login onSuccess={() => setView('dashboard')} />;

  return (
    <Shell view={view} setView={setView}>
      {/* provisional hasta portar las vistas reales */}
      <div style={{ fontFamily: 'Manrope, sans-serif', color: '#41524a' }}>
        Vista actual: <b>{view}</b>
      </div>
    </Shell>
  );
}