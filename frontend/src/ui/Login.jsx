import React, { useState } from 'react';
import { Rombo } from './Rombo';
import { Rings } from './Rings';
import { useIsMobile } from './useIsMobile';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth';
 
const C = {
  petroleo: '#13694A', petroleoDeep: '#0f5840', naranja: '#F0A02A', verde: '#8FBE3D',
  azul: '#2E93C9', ink: '#16261f', ink2: '#41524a',
};
const FD = 'Sora, sans-serif', FB = 'Manrope, sans-serif';
const PROJECT_NAME = 'Evaluación de fuentes generadoras de contaminación sonora en la UPC';
 
function Eye({ open, color }) {
  return open ? (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" />
    </svg>
  ) : (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 3l18 18M10.6 10.7a3 3 0 0 0 4.2 4.2M9.4 5.2A9.7 9.7 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3 3.8M6.1 6.2A17 17 0 0 0 2 12s3.5 7 10 7a9.6 9.6 0 0 0 3.3-.6" />
    </svg>
  );
}
 
/* medidor dB en vivo (barras animadas via keyframe acu-eq) */
function DbMeter({ value = 62.4, limit = 65 }) {
  const pct = Math.min(100, (value / 120) * 100), limPct = (limit / 120) * 100;
  const bars = [38, 70, 52, 88, 61, 100, 74, 46, 92, 58, 80, 48];
  return (
    <div style={{ width: '100%' }}>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 10, marginBottom: 14 }}>
        <span style={{ fontFamily: FD, fontSize: 52, fontWeight: 700, color: 'rgba(255,255,255,0.92)', lineHeight: 1, letterSpacing: '-0.02em' }}>{value.toFixed(1)}</span>
        <span style={{ fontFamily: FB, fontSize: 18, fontWeight: 600, color: 'rgba(255,255,255,0.55)' }}>dB(A)</span>
        <span style={{ marginLeft: 'auto', fontFamily: FB, fontSize: 12, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase', color: C.naranja, display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ width: 7, height: 7, borderRadius: 99, background: C.naranja, boxShadow: `0 0 8px ${C.naranja}`, animation: 'acu-blink 1.6s ease-in-out infinite' }} />En vivo
        </span>
      </div>
      <div style={{ display: 'flex', alignItems: 'flex-end', gap: 4, height: 46, marginBottom: 16 }}>
        {bars.map((h, i) => (
          <div key={i} style={{ flex: 1, height: `${h}%`, borderRadius: 2, background: `linear-gradient(${C.verde}, ${C.azul})`,
            transformOrigin: 'bottom', animation: `acu-eq 1.${3 + (i % 6)}s ${i * 0.08}s ease-in-out infinite alternate` }} />
        ))}
      </div>
      <div style={{ position: 'relative', height: 8, borderRadius: 99, overflow: 'hidden', background: 'rgba(255,255,255,0.14)' }}>
        <div style={{ position: 'absolute', inset: 0, background: `linear-gradient(90deg, ${C.verde} 0%, ${C.verde} 45%, ${C.naranja} 60%, #d8552f 100%)`, opacity: 0.9 }} />
        <div style={{ position: 'absolute', left: `${pct}%`, top: -3, width: 3, height: 14, background: '#fff', borderRadius: 2, boxShadow: '0 0 0 2px rgba(0,0,0,0.25)' }} />
      </div>
      <div style={{ position: 'relative', marginTop: 7, fontFamily: FB, fontSize: 11.5, color: 'rgba(255,255,255,0.55)', display: 'flex', justifyContent: 'space-between' }}>
        <span>0</span>
        <span style={{ position: 'absolute', left: `${limPct}%`, transform: 'translateX(-50%)', color: 'rgba(255,255,255,0.92)', fontWeight: 600 }}>límite {limit}</span>
        <span>120</span>
      </div>
    </div>
  );
}
 
function FieldA({ label, type = 'text', value, onChange, placeholder, autoFocus, trailing }) {
  const [foc, setFoc] = useState(false);
  return (
    <label style={{ display: 'block' }}>
      <span style={{ display: 'block', fontFamily: FB, fontSize: 13, fontWeight: 600, color: '#41524a', marginBottom: 7 }}>{label}</span>
      <div style={{ position: 'relative', display: 'flex', alignItems: 'center', background: '#fff', borderRadius: 12,
        border: `1.5px solid ${foc ? C.petroleo : 'rgba(19,105,74,0.18)'}`,
        boxShadow: foc ? '0 0 0 4px rgba(19,105,74,0.10)' : '0 1px 2px rgba(20,40,30,0.04)', transition: 'border-color .15s, box-shadow .15s' }}>
        <input type={type} value={value} placeholder={placeholder} autoFocus={autoFocus}
          onChange={(e) => onChange(e.target.value)} onFocus={() => setFoc(true)} onBlur={() => setFoc(false)}
          style={{ flex: 1, border: 'none', outline: 'none', background: 'transparent', padding: '13px 14px', fontFamily: FB, fontSize: 15.5, color: '#16261f', borderRadius: 12 }} />
        {trailing}
      </div>
    </label>
  );
}
 
export function Login({ onSuccess }) {
  const { setSession } = useAuth();
  const isMobile = useIsMobile();
  const [user, setUser] = useState('');
  const [pass, setPass] = useState('');
  const [show, setShow] = useState(false);
  const [remember, setRemember] = useState(true);
  const [status, setStatus] = useState('idle'); // idle | loading | error | ok
  const [err, setErr] = useState('');
 
  async function submit(e) {
    e.preventDefault();
    if (!user.trim() || !pass.trim()) { setStatus('error'); setErr(!user.trim() ? 'Ingresa tu usuario o correo' : 'Ingresa tu contraseña'); return; }
    setErr(''); setStatus('loading');
    try {
      const res = await api.post('/auth/login', { usernameOrEmail: user.trim(), password: pass });
      const token = res.token || res.accessToken;
      localStorage.setItem('acu-token', token);            // ← token guardado aquí
      setSession?.(res);                                    // guarda usuario/rol en el contexto
      setStatus('ok');
      onSuccess?.();
    } catch (e2) {
      setStatus('error'); setErr('Usuario o contraseña incorrectos');
    }
  }
 
  const Panel = (
    <div style={{ position: 'relative', overflow: 'hidden', background: '#0f5840',
      backgroundImage: 'radial-gradient(120% 120% at 18% 12%, #1a7a58 0%, #0f5840 48%, #0a4131 100%)',
      color: '#fff', flex: isMobile ? 'none' : '1 1 56%', height: isMobile ? 200 : 'auto',
      padding: isMobile ? '22px 26px' : '52px 56px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
      <div style={{ position: 'absolute', right: isMobile ? '-30%' : '-14%', top: isMobile ? '-60%' : '8%', pointerEvents: 'none', opacity: 0.5 }}>
        <Rings color={C.verde} count={isMobile ? 4 : 6} base={isMobile ? 50 : 70} gap={isMobile ? 40 : 60} opacity={0.5} />
      </div>
      <div style={{ position: 'relative', zIndex: 1, display: 'flex', alignItems: 'center', gap: 13 }}>
        <Rombo size={isMobile ? 34 : 44} gap="#0f5840" />
        <div>
          <div style={{ fontFamily: FD, fontWeight: 700, fontSize: isMobile ? 18 : 21, letterSpacing: '-0.01em' }}>AcústicaUPC</div>
          <div style={{ fontFamily: FB, fontSize: 11.5, fontWeight: 600, letterSpacing: '0.14em', textTransform: 'uppercase', color: 'rgba(255,255,255,0.62)' }}>Sistema BI · Monitoreo de ruido</div>
        </div>
      </div>
      {!isMobile && (
        <div style={{ position: 'relative', zIndex: 1, maxWidth: 460 }}>
          <h2 style={{ fontFamily: FD, fontWeight: 600, fontSize: 27, lineHeight: 1.2, letterSpacing: '-0.02em', margin: '0 0 28px' }}>{PROJECT_NAME}</h2>
          <div style={{ background: 'rgba(255,255,255,0.07)', border: '1px solid rgba(255,255,255,0.12)', borderRadius: 18, padding: '22px 24px', backdropFilter: 'blur(4px)' }}>
            <div style={{ fontFamily: FB, fontSize: 12.5, fontWeight: 600, letterSpacing: '0.05em', color: 'rgba(255,255,255,0.6)', marginBottom: 14, textTransform: 'uppercase' }}>Estación Centro · UPC Valledupar</div>
            <DbMeter value={62.4} limit={65} />
          </div>
        </div>
      )}
      <div style={{ position: 'relative', zIndex: 1, display: isMobile ? 'none' : 'flex', alignItems: 'center', gap: 9, fontFamily: FB, fontSize: 12.5, color: 'rgba(255,255,255,0.7)' }}>
        <span style={{ display: 'inline-flex', padding: '5px 11px', borderRadius: 99, background: 'rgba(240,160,42,0.16)', border: '1px solid rgba(240,160,42,0.35)', color: C.naranja, fontWeight: 600 }}>Resolución 0627 de 2006</span>
        Estándares máximos permisibles de ruido ambiental
      </div>
    </div>
  );
 
  const Form = (
    <div style={{ flex: isMobile ? 'none' : '1 1 44%', background: '#F7F5EF',
      padding: isMobile ? '32px 26px 40px' : '56px clamp(40px, 6vw, 76px)', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
      <div style={{ width: '100%', maxWidth: 380, margin: '0 auto' }}>
        <h1 style={{ fontFamily: FD, fontWeight: 700, fontSize: isMobile ? 26 : 30, color: '#13231c', letterSpacing: '-0.02em', margin: '0 0 6px' }}>Iniciar sesión</h1>
        <p style={{ fontFamily: FB, fontSize: 15, color: '#5a6962', margin: '0 0 30px' }}>Accede al panel de monitoreo acústico.</p>
        <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
          <FieldA label="Usuario o correo" value={user} onChange={setUser} placeholder="usuario o correo" autoFocus={!isMobile} />
          <FieldA label="Contraseña" type={show ? 'text' : 'password'} value={pass} onChange={setPass} placeholder="••••••••"
            trailing={
              <button type="button" onClick={() => setShow((s) => !s)} aria-label="Mostrar contraseña"
                style={{ border: 'none', background: 'transparent', cursor: 'pointer', padding: '0 13px', display: 'flex', alignItems: 'center', color: '#7c8c84' }}>
                <Eye open={show} color="#7c8c84" />
              </button>
            } />
          {status === 'error' && <div style={{ fontFamily: FB, fontSize: 13.5, color: '#c0492a', fontWeight: 600, marginTop: -4 }}>{err}</div>}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontFamily: FB, fontSize: 13.5 }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', color: '#41524a', fontWeight: 500 }}>
              <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} style={{ width: 16, height: 16, accentColor: C.petroleo }} />
              Recordar sesión
            </label>
            <a href="#" onClick={(e) => e.preventDefault()} style={{ color: C.petroleo, fontWeight: 600, textDecoration: 'none' }}>¿Olvidaste tu contraseña?</a>
          </div>
          <button type="submit" disabled={status === 'loading'}
            style={{ marginTop: 6, border: 'none', cursor: status === 'loading' ? 'default' : 'pointer', borderRadius: 12, padding: 15,
              background: status === 'loading' ? '#2f6b53' : C.petroleo, color: '#fff', fontFamily: FD, fontWeight: 600, fontSize: 16,
              boxShadow: '0 8px 20px rgba(19,105,74,0.28)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10 }}>
            {status === 'loading'
              ? <><span style={{ width: 16, height: 16, border: '2.5px solid rgba(255,255,255,0.4)', borderTopColor: '#fff', borderRadius: 99, animation: 'acu-spin .7s linear infinite' }} />Verificando…</>
              : 'Ingresar al panel'}
          </button>
        </form>
        <div style={{ marginTop: 36, paddingTop: 20, borderTop: '1px solid rgba(19,105,74,0.12)', fontFamily: FB, fontSize: 12, color: '#8a978f', lineHeight: 1.6 }}>
          Universidad Popular del Cesar<br />Facultad de Ingeniería · Ambiental y Sanitaria
        </div>
      </div>
    </div>
  );
 
  return (
    <div style={{ width: '100%', minHeight: '100vh', overflow: 'hidden', display: 'flex', flexDirection: isMobile ? 'column' : 'row', background: '#F7F5EF' }}>
      {Panel}{Form}
    </div>
  );
}
 