import React, { useState, useEffect } from 'react';
import { Rombo } from './Rombo';
import { Radar } from './Rings';
import { Icon, icons } from './primitives';
import { useIsMobile } from './useIsMobile';
import { useAuth } from '@/lib/auth';

const C = {
  petroleo: '#13694A', petroleoDeep: '#0f5840', petroleoDeeper: '#0a3327',
  naranja: '#F0A02A', verde: '#8FBE3D', paper: '#F4F1EA',
  ink: '#15241d', ink2: '#41524a', muted: '#8a978f', line: 'rgba(19,105,74,0.12)',
};
const FD = 'Sora, sans-serif', FB = 'Manrope, sans-serif';

const NAV = [
  { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
  { id: 'zonas', label: 'Zonas', icon: 'zonas', soon: true },
  { id: 'ingesta', label: 'Ingesta', icon: 'ingesta' },
  { id: 'cumplimiento', label: 'Cumplimiento', icon: 'cumplimiento' },
  { id: 'usuarios', label: 'Usuarios', icon: 'usuarios' },
];

function NavItem({ item, active, onClick }) {
  const [h, setH] = useState(false);
  const dim = item.soon;
  return (
    <button onClick={() => !item.soon && onClick(item.id)} onMouseEnter={() => setH(true)} onMouseLeave={() => setH(false)}
      style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 12, width: '100%', textAlign: 'left',
        border: 'none', cursor: item.soon ? 'default' : 'pointer', fontFamily: FB, fontSize: 14.5, fontWeight: active ? 700 : 500,
        padding: '11px 14px', borderRadius: 11, color: active ? '#fff' : (dim ? 'rgba(255,255,255,0.4)' : 'rgba(255,255,255,0.78)'),
        background: active ? 'rgba(255,255,255,0.12)' : (h && !item.soon ? 'rgba(255,255,255,0.06)' : 'transparent'), transition: 'background .14s, color .14s' }}>
      {active && <span style={{ position: 'absolute', left: -10, top: 9, bottom: 9, width: 3.5, borderRadius: 99, background: C.naranja }} />}
      <Icon d={icons[item.icon]} size={19} stroke={active ? C.verde : 'currentColor'} />
      <span style={{ flex: 1 }}>{item.label}</span>
      {item.soon && <span style={{ fontSize: 10.5, fontWeight: 700, letterSpacing: '0.04em', color: 'rgba(255,255,255,0.55)', background: 'rgba(255,255,255,0.1)', padding: '2px 7px', borderRadius: 99 }}>próx.</span>}
    </button>
  );
}

function Sidebar({ view, setView, onNavigate }) {
  return (
    <div style={{ position: 'relative', height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden',
      background: `radial-gradient(120% 80% at 20% 0%, ${C.petroleoDeep} 0%, #0c4836 45%, ${C.petroleoDeeper} 100%)`, color: '#fff' }}>
      <div style={{ position: 'absolute', bottom: -70, left: -50, opacity: 0.5, pointerEvents: 'none' }}>
        <Radar color={C.verde} size={220} count={3} />
      </div>
      <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 11, padding: '24px 22px 22px' }}>
        <Rombo size={30} gap="#0c4836" />
        <span style={{ fontFamily: FD, fontWeight: 700, fontSize: 19, letterSpacing: '-0.01em' }}>AcústicaUPC</span>
      </div>
      <nav style={{ position: 'relative', display: 'flex', flexDirection: 'column', gap: 3, padding: '6px 22px', flex: 1 }}>
        {NAV.map((it) => <NavItem key={it.id} item={it} active={view === it.id} onClick={(id) => { setView(id); onNavigate && onNavigate(); }} />)}
      </nav>
      <div style={{ position: 'relative', padding: '18px 24px', fontFamily: FB, fontSize: 11.5, lineHeight: 1.6, color: 'rgba(255,255,255,0.42)', borderTop: '1px solid rgba(255,255,255,0.08)' }}>
        AcústicaUPC · v1.0.0<br />Universidad Popular del Cesar · 2026
      </div>
    </div>
  );
}

function Topbar({ onMenu, isMobile }) {
  const { user, role, logout } = useAuth();
  const displayName = user?.name || user?.username || 'Usuario';
  return (
    <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 14,
      height: 64, flexShrink: 0, padding: isMobile ? '0 16px' : '0 30px', background: 'rgba(244,241,234,0.85)',
      backdropFilter: 'blur(10px)', borderBottom: `1px solid ${C.line}`, position: 'sticky', top: 0, zIndex: 20 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, minWidth: 0 }}>
        {isMobile && (
          <button onClick={onMenu} aria-label="Menú" style={{ display: 'flex', border: 'none', background: 'transparent', cursor: 'pointer', color: C.ink, padding: 4 }}>
            <Icon d={icons.menu} size={24} />
          </button>
        )}
        {isMobile && <Rombo size={24} gap={C.paper} />}
        <span style={{ fontFamily: FD, fontWeight: 600, fontSize: isMobile ? 15 : 17, color: C.ink, letterSpacing: '-0.01em', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {isMobile ? 'AcústicaUPC' : 'Panel de monitoreo acústico'}
        </span>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: isMobile ? 12 : 22 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 36, height: 36, borderRadius: 99, background: 'rgba(19,105,74,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: C.petroleo }}>
            <Icon d={icons.user} size={19} />
          </div>
          {!isMobile && (
            <div style={{ lineHeight: 1.2 }}>
              <div style={{ fontFamily: FD, fontWeight: 700, fontSize: 14, color: C.ink }}>{displayName}</div>
              <div style={{ fontFamily: FB, fontSize: 11.5, fontWeight: 600, letterSpacing: '0.08em', color: C.muted }}>{role}</div>
            </div>
          )}
        </div>
        <button onClick={logout} style={{ display: 'flex', alignItems: 'center', gap: 7, border: 'none', background: 'transparent', cursor: 'pointer',
          fontFamily: FB, fontSize: 14, fontWeight: 600, color: C.ink2 }}>
          <Icon d={icons.logout} size={18} stroke={C.petroleo} />{!isMobile && 'Salir'}
        </button>
      </div>
    </header>
  );
}

export function Shell({ view, setView, children }) {
  const isMobile = useIsMobile();
  const [drawer, setDrawer] = useState(false);
  useEffect(() => { if (!isMobile) setDrawer(false); }, [isMobile]);

  return (
    <div style={{ height: '100vh', display: 'flex', background: C.paper, overflow: 'hidden' }}>
      {!isMobile && (
        <aside style={{ width: 250, flexShrink: 0, height: '100%' }}><Sidebar view={view} setView={setView} /></aside>
      )}
      {isMobile && drawer && (
        <div onClick={() => setDrawer(false)} style={{ position: 'fixed', inset: 0, zIndex: 40, background: 'rgba(10,30,22,0.5)', backdropFilter: 'blur(2px)' }}>
          <aside onClick={(e) => e.stopPropagation()} style={{ width: 264, height: '100%', boxShadow: '20px 0 60px rgba(0,0,0,0.3)' }}>
            <Sidebar view={view} setView={setView} onNavigate={() => setDrawer(false)} />
          </aside>
        </div>
      )}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, height: '100%' }}>
        <Topbar isMobile={isMobile} onMenu={() => setDrawer(true)} />
        <main style={{ flex: 1, overflowY: 'auto', padding: isMobile ? '20px 16px 60px' : '30px 36px 64px',
          background: 'radial-gradient(135% 100% at 100% 0%, #fbfaf6 0%, #f4f1ea 45%, #f1ede4 100%)' }}>
          <div style={{ maxWidth: 1180, margin: '0 auto' }}>{children}</div>
        </main>
      </div>
    </div>
  );
}