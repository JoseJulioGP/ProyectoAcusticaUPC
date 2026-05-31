import React, { useState, useEffect } from 'react';

/* ── Icons ─────────────────────────────────────────────────────────────
   Tokens del paso 1: petroleo, rombo.{verde,azul,cafe,naranja}, paper,
   ink, ink2, muted, danger, dangerDeep, warn · font-display / font-body */
export const Icon = ({ d, size = 19, fill = false, stroke = 'currentColor', sw = 1.7 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill ? stroke : 'none'} stroke={fill ? 'none' : stroke}
    strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round">{d}</svg>
);

export const icons = {
  dashboard: <><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /></>,
  zonas: <><path d="M12 21s7-6.5 7-12a7 7 0 1 0-14 0c0 5.5 7 12 7 12Z" /><circle cx="12" cy="9" r="2.5" /></>,
  ingesta: <><path d="M12 16V4m0 0L7 9m5-5 5 5" /><path d="M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" /></>,
  cumplimiento: <><path d="M12 3l8 3v5c0 5-3.5 8.5-8 10-4.5-1.5-8-5-8-10V6l8-3Z" /><path d="M9 12l2 2 4-4" /></>,
  usuarios: <><circle cx="9" cy="8" r="3.2" /><path d="M3.5 20a5.5 5.5 0 0 1 11 0" /><path d="M16 5.5a3 3 0 0 1 0 5.4M16.5 20a5.5 5.5 0 0 0-2.3-4.5" /></>,
  logout: <><path d="M15 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3" /><path d="M10 17l-5-5 5-5M5 12h11" /></>,
  user: <><circle cx="12" cy="8" r="4" /><path d="M5 21a7 7 0 0 1 14 0" /></>,
  menu: <><path d="M4 7h16M4 12h16M4 17h16" /></>,
  pdf: <><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6Z" /><path d="M14 2v6h6" /><path d="M9 13h6M9 17h4" /></>,
  close: <><path d="M6 6l12 12M18 6 6 18" /></>,
  trash: <><path d="M4 7h16M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2m-9 0 1 13a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2l1-13" /></>,
  eye: <><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" /></>,
  retry: <><path d="M21 12a9 9 0 1 1-3-6.7M21 4v4h-4" /></>,
  search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></>,
};

/* ── Card ──────────────────────────────────────────────────────────────── */
export function Card({ children, className = '', pad = 22, accent, lift, style }) {
  return (
    <div
      className={
        'acu-card rounded-xl2 bg-white/95 backdrop-blur-[10px] border border-white/70 ' +
        '[outline:1px_solid_rgba(19,105,74,0.07)] [outline-offset:-1px] transition ' +
        'shadow-[0_2px_6px_rgba(20,40,30,0.04),0_14px_34px_rgba(20,40,30,0.06)] ' +
        (lift ? 'hover:-translate-y-[3px] hover:shadow-[0_18px_44px_rgba(20,40,30,0.13),0_3px_10px_rgba(20,40,30,0.05)] ' : '') +
        className
      }
      style={{ padding: pad, ...(accent ? { borderTop: `3px solid ${accent}` } : null), ...style }}
    >
      {children}
    </div>
  );
}

/* ── Badge ─────────────────────────────────────────────────────────────── */
const BADGE = {
  Completado: 'bg-rombo-verde/15 text-[#41691f]',
  Procesando: 'bg-rombo-naranja/15 text-[#9a6212]',
  Fallido:    'bg-danger/15 text-dangerDeep',
  CUMPLE:     'bg-rombo-verde/15 text-[#41691f]',
  NO_CUMPLE:  'bg-danger/15 text-dangerDeep',
  Cumple:     'bg-rombo-verde/15 text-[#41691f]',
  Excede:     'bg-danger/15 text-dangerDeep',
  LEVE:       'bg-warn/15 text-[#8a5e0a]',
  MODERADA:   'bg-rombo-naranja/15 text-[#9a6212]',
  CRITICA:    'bg-danger/15 text-dangerDeep',
};
const DOT = {
  Completado: 'bg-rombo-verde', Procesando: 'bg-rombo-naranja', Fallido: 'bg-danger',
  CUMPLE: 'bg-rombo-verde', NO_CUMPLE: 'bg-danger', Cumple: 'bg-rombo-verde', Excede: 'bg-danger',
  LEVE: 'bg-warn', MODERADA: 'bg-rombo-naranja', CRITICA: 'bg-danger',
};
export function Badge({ kind, children, alert }) {
  const danger = kind === 'Excede' || kind === 'NO_CUMPLE' || kind === 'CRITICA';
  const spin = kind === 'Procesando';
  const base = 'inline-flex items-center gap-1.5 font-body text-xs font-bold tracking-[0.02em] px-[11px] py-1 rounded-full whitespace-nowrap';
  return (
    <span className={
      base + ' ' + (danger
        ? 'text-white bg-gradient-to-r from-danger to-rombo-naranja shadow-[0_2px_8px_rgba(207,79,44,0.3)] ' + (alert ? 'animate-[acu-blink_1.8s_ease-in-out_infinite]' : '')
        : (BADGE[kind] || BADGE.Completado))
    }>
      {spin
        ? <span className="w-[9px] h-[9px] rounded-full border-2 border-[rgba(154,98,18,0.35)] border-t-rombo-naranja animate-[acu-spin_.7s_linear_infinite]" />
        : <span className={'w-1.5 h-1.5 rounded-full ' + (danger ? 'bg-white' : (DOT[kind] || 'bg-rombo-verde'))} />}
      {children || kind}
    </span>
  );
}

/* ── Button ────────────────────────────────────────────────────────────── */
const BTN = {
  primary: 'bg-petroleo text-white shadow-[0_6px_16px_rgba(19,105,74,0.22)]',
  naranja: 'bg-rombo-naranja text-[#2a1c06] shadow-[0_6px_16px_rgba(240,160,42,0.28)]',
  ghost:   'bg-transparent text-petroleo border-[1.5px] border-petroleo/15',
  soft:    'bg-petroleo/[0.08] text-petroleo',
  disabled:'bg-[#e6e2d8] text-[#a9a293] cursor-not-allowed',
};
export function Button({ children, variant = 'primary', icon, onClick, type = 'button', disabled, className = '', style }) {
  const base = 'relative overflow-hidden inline-flex items-center justify-center gap-2 font-display font-semibold text-[14.5px] rounded-[11px] px-[18px] py-[11px] border-0 whitespace-nowrap transition active:scale-[.98]';
  return (
    <button type={type} onClick={disabled ? undefined : onClick} disabled={disabled}
      className={base + ' ' + (disabled ? BTN.disabled : 'acu-btn ' + BTN[variant]) + ' ' + className} style={style}>
      {!disabled && <span className="acu-shine" />}
      {icon && <Icon d={icons[icon]} size={17} />}{children}
    </button>
  );
}

/* ── Field + inputs ──────────────────────────────────────────────────────── */
export function Field({ label, children, className = '' }) {
  return (
    <label className={'flex flex-col gap-[7px] min-w-0 ' + className}>
      {label && <span className="font-body text-[12.5px] font-bold text-ink2 tracking-[0.01em]">{label}</span>}
      {children}
    </label>
  );
}

const INPUT = 'font-body text-[14.5px] text-ink bg-white/90 border-[1.5px] border-petroleo/15 rounded-[11px] px-[13px] py-[11px] w-full box-border outline-none transition ' +
  'focus:border-rombo-verde focus:shadow-[0_0_0_3px_rgba(143,190,61,0.35),0_0_14px_rgba(143,190,61,0.2)]';

export function TextInput({ className = '', ...props }) {
  return <input {...props} className={INPUT + ' ' + className} />;
}

export function Select({ value, onChange, options, className = '' }) {
  return (
    <div className="relative">
      <select value={value} onChange={(e) => onChange(e.target.value)}
        className={INPUT + ' appearance-none pr-[34px] cursor-pointer ' + className}>
        {options.map((o) => <option key={o.value ?? o} value={o.value ?? o}>{o.label ?? o}</option>)}
      </select>
      <svg width="12" height="12" viewBox="0 0 12 12" className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none"
        fill="none" stroke="#8a978f" strokeWidth="1.8" strokeLinecap="round"><path d="M2 4l4 4 4-4" /></svg>
    </div>
  );
}

/* ── PageHead ──────────────────────────────────────────────────────────── */
export function PageHead({ title, sub, right }) {
  return (
    <div className="flex items-start justify-between gap-4 flex-wrap mb-[26px]">
      <div>
        <h1 className="font-display font-bold text-[27px] text-ink tracking-[-0.02em] m-0">{title}</h1>
        {sub && <p className="font-body text-[14.5px] text-ink2 mt-[7px] max-w-[620px] leading-[1.5]">{sub}</p>}
      </div>
      {right}
    </div>
  );
}

/* ── CountUp ───────────────────────────────────────────────────────────── */
export function CountUp({ value, decimals = 0, suffix = '', duration = 1100, locale = 'es' }) {
  const [n, setN] = useState(0);
  useEffect(() => {
    let raf, start; const from = 0, to = value;
    const tick = (t) => {
      if (!start) start = t;
      const p = Math.min(1, (t - start) / duration);
      const e = 1 - Math.pow(1 - p, 3);
      setN(from + (to - from) * e);
      if (p < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [value]);
  const txt = decimals ? n.toFixed(decimals) : Math.round(n).toLocaleString(locale);
  return <span>{txt}{suffix}</span>;
}
