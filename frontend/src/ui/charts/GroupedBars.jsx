import React from 'react';

/**
 * Barras agrupadas diurno/nocturno por zona, con animacion `acu-bargrow`.
 *
 * Props:
 *  - data: [{ zone: string, diurno: number, nocturno: number }]
 *  - height?: number  (default 220)
 */
export function GroupedBars({ data = [], height = 220 }) {
  const W = 600;
  const H = height;
  const padL = 36, padR = 12, padT = 18, padB = 36;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;

  const allV = data.flatMap((d) => [d.diurno, d.nocturno]).filter((v) => Number.isFinite(v));
  const max = allV.length ? Math.max(...allV, 1) : 1;

  const groupW = data.length ? innerW / data.length : innerW;
  const barW = Math.min(22, Math.max(8, (groupW - 14) / 2));

  return (
    <svg width="100%" viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Comparacion por zona (diurno / nocturno)">
      {[0, 0.5, 1].map((p, i) => (
        <line key={i} x1={padL} y1={padT + innerH * (1 - p)} x2={padL + innerW} y2={padT + innerH * (1 - p)}
          stroke="#e1ddd2" strokeWidth="1" />
      ))}
      {[0, 0.5, 1].map((p, i) => (
        <text key={i} x={padL - 6} y={padT + innerH * (1 - p) + 4}
          textAnchor="end" fontFamily="Manrope, sans-serif" fontSize="10" fill="#8a978f">
          {(max * p).toFixed(0)}
        </text>
      ))}
      {data.map((d, i) => {
        const cx = padL + groupW * i + groupW / 2;
        const baseY = padT + innerH;
        const hDi = Math.max(0, ((d.diurno ?? 0) / max) * innerH);
        const hNo = Math.max(0, ((d.nocturno ?? 0) / max) * innerH);
        const xDi = cx - barW - 2;
        const xNo = cx + 2;
        return (
          <g key={i}>
            <rect x={xDi} y={baseY - hDi} width={barW} height={hDi} rx="3"
              fill="#F0A02A"
              style={{ transformOrigin: `${xDi}px ${baseY}px`, animation: `acu-bargrow .9s ${i * 0.08}s cubic-bezier(.22,.7,.3,1) both` }} />
            <rect x={xNo} y={baseY - hNo} width={barW} height={hNo} rx="3"
              fill="#2E93C9"
              style={{ transformOrigin: `${xNo}px ${baseY}px`, animation: `acu-bargrow .9s ${i * 0.08 + 0.05}s cubic-bezier(.22,.7,.3,1) both` }} />
            <text x={cx} y={H - 18} textAnchor="middle" fontFamily="Manrope, sans-serif"
              fontSize="10" fill="#41524a">{trim(d.zone, 14)}</text>
          </g>
        );
      })}
    </svg>
  );
}

function trim(s, n) {
  if (!s) return '';
  return s.length > n ? s.slice(0, n - 1) + '…' : s;
}
