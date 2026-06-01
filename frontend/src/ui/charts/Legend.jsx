import React from 'react';

/**
 * Leyenda horizontal de colores.
 * Props:
 *  - items: [{ color: string, label: string }]
 */
export function Legend({ items = [] }) {
  return (
    <div className="flex flex-wrap gap-3 font-body text-[12px] text-ink2">
      {items.map((it, i) => (
        <span key={i} className="inline-flex items-center gap-1.5">
          <span className="inline-block w-3 h-3 rounded-sm" style={{ background: it.color }} />
          {it.label}
        </span>
      ))}
    </div>
  );
}
