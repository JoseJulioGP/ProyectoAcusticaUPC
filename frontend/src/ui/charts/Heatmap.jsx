import React from 'react';

const LEVEL_STYLE = {
  ok:        { bg: 'rgba(143,190,61,0.18)', fg: '#41691f' },
  moderate:  { bg: 'rgba(240,160,42,0.22)', fg: '#9a6212' },
  high:      { bg: 'rgba(207,79,44,0.22)', fg: '#b23d1e' },
  critical:  { bg: '#cf4f2c',               fg: '#ffffff' },
  empty:     { bg: '#eee9dc',               fg: '#a8a395' },
};

/**
 * Heatmap 24h x zona.
 *
 * Props:
 *  - zones: string[]                                  (filas)
 *  - cells: { "<zona>|<hora>": { value, level } }     (level ∈ ok|moderate|high|critical)
 */
export function Heatmap({ zones = [], cells = {} }) {
  const hours = Array.from({ length: 24 }, (_, i) => i);

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-[11px] font-body border-collapse">
        <thead>
          <tr>
            <th className="text-left font-bold text-ink2 pr-2 py-1.5 sticky left-0 bg-paper">
              Zona / hora
            </th>
            {hours.map((h) => (
              <th key={h} className="text-center font-semibold text-muted py-1.5 min-w-[26px]">{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {zones.map((z) => (
            <tr key={z}>
              <td className="font-semibold text-ink2 pr-2 py-1 sticky left-0 bg-paper whitespace-nowrap">
                {z}
              </td>
              {hours.map((h) => {
                const c = cells[`${z}|${h}`];
                const lvl = c?.level ?? 'empty';
                const col = LEVEL_STYLE[lvl] ?? LEVEL_STYLE.empty;
                return (
                  <td key={h} className="text-center align-middle p-0.5">
                    <div
                      title={c ? `${z} · ${h}h · ${c.value} dB` : 'sin datos'}
                      className="rounded-[5px] py-1 px-1 font-bold"
                      style={{ background: col.bg, color: col.fg }}>
                      {c && Number.isFinite(c.value) ? Math.round(c.value) : '—'}
                    </div>
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
