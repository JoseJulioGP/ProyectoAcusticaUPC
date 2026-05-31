import React from 'react';

/* Ondas de propagación — círculos concéntricos que evocan el sonido expandiéndose */
export function Rings({ color = '#fff', count = 5, base = 40, gap = 34, opacity = 0.5, style }) {
  const max = base + gap * (count - 1), vb = max * 2 + 6;
  return (
    <svg width={vb} height={vb} viewBox={`0 0 ${vb} ${vb}`} style={style} aria-hidden="true">
      {Array.from({ length: count }).map((_, i) => (
        <circle key={i} cx={vb / 2} cy={vb / 2} r={base + gap * i} fill="none" stroke={color}
          strokeWidth={i === 0 ? 2.2 : 1.3} style={{ opacity: opacity * (1 - i / (count + 1)) }} />
      ))}
      <circle cx={vb / 2} cy={vb / 2} r="5" fill={color} style={{ opacity: opacity + 0.3 }} />
    </svg>
  );
}

/* Radar pulsante — anillos que se expanden de forma infinita (usa el keyframe acu-radar de index.css) */
export function Radar({ color = '#8FBE3D', size = 200, count = 3, style }) {
  return (
    <div style={{ position: 'relative', width: size, height: size, ...style }} aria-hidden="true">
      {Array.from({ length: count }).map((_, i) => (
        <span key={i} style={{ position: 'absolute', inset: 0, borderRadius: '50%', border: `1.5px solid ${color}`,
          transformOrigin: 'center', animation: `acu-radar 3.6s ${i * 1.2}s ease-out infinite` }} />
      ))}
      <span style={{ position: 'absolute', top: '50%', left: '50%', width: 6, height: 6, borderRadius: 99, background: color, transform: 'translate(-50%,-50%)', boxShadow: `0 0 10px ${color}` }} />
    </div>
  );
}