import React from 'react';

/**
 * Line chart con animacion `acu-draw` (definida en index.css).
 * Cero dependencias: SVG puro.
 *
 * Props:
 *  - series: [{ name: string, color: string, points: number[] }]
 *  - xLabels: string[]      (alineados con points por indice)
 *  - standard?: number      (linea horizontal punteada del estandar)
 *  - height?: number        (default 220)
 */
export function LineChart({ series = [], xLabels = [], standard, height = 220 }) {
  const W = 600;
  const H = height;
  const padL = 36, padR = 12, padT = 18, padB = 28;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;

  const allValues = series.flatMap((s) => s.points).filter((v) => Number.isFinite(v));
  if (standard != null && Number.isFinite(standard)) allValues.push(standard);
  const min = allValues.length ? Math.min(...allValues) : 0;
  const max = allValues.length ? Math.max(...allValues) : 1;
  const range = Math.max(1, max - min);

  const xAt = (i, n) => padL + (n <= 1 ? innerW / 2 : (i / (n - 1)) * innerW);
  const yAt = (v) => padT + innerH - ((v - min) / range) * innerH;

  return (
    <svg width="100%" viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Grafica de lineas">
      {[0, 0.25, 0.5, 0.75, 1].map((p, i) => (
        <line key={i} x1={padL} y1={padT + innerH * p} x2={padL + innerW} y2={padT + innerH * p}
          stroke="#e1ddd2" strokeWidth="1" />
      ))}
      {[0, 0.5, 1].map((p, i) => {
        const v = max - (max - min) * p;
        return (
          <text key={i} x={padL - 6} y={padT + innerH * p + 4}
            textAnchor="end" fontFamily="Manrope, sans-serif" fontSize="10" fill="#8a978f">
            {v.toFixed(0)}
          </text>
        );
      })}
      {xLabels.map((lbl, i) => (
        <text key={i} x={xAt(i, xLabels.length)} y={H - 8}
          textAnchor="middle" fontFamily="Manrope, sans-serif" fontSize="10" fill="#8a978f">
          {lbl}
        </text>
      ))}
      {standard != null && Number.isFinite(standard) && (
        <g>
          <line x1={padL} y1={yAt(standard)} x2={padL + innerW} y2={yAt(standard)}
            stroke="#cf4f2c" strokeWidth="1.6" strokeDasharray="6 4" />
          <text x={padL + innerW - 4} y={yAt(standard) - 4} textAnchor="end"
            fontFamily="Manrope, sans-serif" fontSize="10" fontWeight="700" fill="#cf4f2c">
            estandar {standard} dB
          </text>
        </g>
      )}
      {series.map((s, idx) => {
        const pts = s.points.map((v, i) => `${xAt(i, s.points.length)},${yAt(v)}`).join(' ');
        // Longitud aproximada para la animacion de trazado; suficiente para los rangos esperados.
        const approxLen = 2000;
        return (
          <polyline key={idx} points={pts} fill="none" stroke={s.color} strokeWidth="2.2"
            strokeLinecap="round" strokeLinejoin="round"
            style={{
              strokeDasharray: approxLen,
              strokeDashoffset: approxLen,
              animation: `acu-draw 1.2s ${idx * 0.15}s cubic-bezier(.22,.7,.3,1) forwards`,
            }} />
        );
      })}
    </svg>
  );
}
