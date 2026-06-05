import { useState } from 'react';
import { useHeatmap } from '../hooks/useHeatmap';
import { Card } from '@/ui/primitives';

function colorFor(excess) {
  if (excess === -1 || excess == null) return 'bg-slate-100 text-muted';
  if (excess <= 0) return 'bg-rombo-verde/20 text-[#3a6018]';
  if (excess <= 3) return 'bg-warn/20 text-[#7a5500]';
  if (excess <= 8) return 'bg-rombo-naranja/20 text-[#8a4a00]';
  return 'bg-danger/20 text-dangerDeep';
}

const LEGEND = [
  { cls: 'bg-rombo-verde/20 text-[#3a6018]',   dot: 'bg-rombo-verde',   label: 'Cumple' },
  { cls: 'bg-warn/20 text-[#7a5500]',           dot: 'bg-warn',          label: '≤3 dB' },
  { cls: 'bg-rombo-naranja/20 text-[#8a4a00]',  dot: 'bg-rombo-naranja', label: '3–8 dB' },
  { cls: 'bg-danger/20 text-dangerDeep',         dot: 'bg-danger',        label: '>8 dB' },
];

export default function HeatmapGrid() {
  const [day, setDay] = useState(''); // '' = rango global; 'yyyy-mm-dd' = ese día
  const override = day
    ? { from: `${day}T00:00:00-05:00`, to: `${day}T23:59:59-05:00` }
    : {};
  const { data, loading, error } = useHeatmap(override);

  return (
    <Card className="overflow-x-auto">
      <div className="flex items-start justify-between gap-4 flex-wrap mb-3">
        <h3 className="font-display text-sm font-semibold text-ink">
          Mapa de calor — LAeq por zona y hora del día
          {day && (
            <span className="ml-2 text-xs font-normal text-muted">
              · {new Date(`${day}T00:00`).toLocaleDateString('es-CO', { dateStyle: 'long' })}
            </span>
          )}
        </h3>
        <div className="flex flex-wrap gap-2">
          {LEGEND.map(({ cls, dot, label }) => (
            <span key={label}
              className={`inline-flex items-center gap-1.5 text-xs font-body px-2.5 py-1 rounded-full font-semibold ${cls}`}>
              <span className={`w-2 h-2 rounded-full inline-block ${dot}`} />
              {label}
            </span>
          ))}
        </div>
      </div>

      {/* Selector de día compacto */}
      <div className="flex items-center gap-2 mb-3">
        <label className="text-xs font-medium text-slate-500">Día</label>
        <input
          type="date"
          value={day}
          onChange={(e) => setDay(e.target.value)}
          className="rounded-lg border border-slate-300 px-2.5 py-1.5 text-sm text-slate-700 focus:border-petroleo focus:outline-none focus:ring-2 focus:ring-petroleo/20"
        />
        {day && (
          <button
            type="button"
            onClick={() => setDay('')}
            className="text-xs font-medium text-petroleo hover:underline"
          >
            Ver rango completo
          </button>
        )}
      </div>

      <div className="overflow-x-auto">
        {loading ? (
          <div className="h-72 bg-slate-100 animate-pulse rounded-xl2" />
        ) : error ? (
          <div className="h-72 flex items-center justify-center text-danger text-sm">
            Error al cargar el mapa
          </div>
        ) : !data || data.zoneNames.length === 0 ? (
          <div className="h-72 flex items-center justify-center text-muted text-sm">
            Sin datos para el {day ? 'día seleccionado' : 'mapa de calor'}
          </div>
        ) : (
          <table className="text-xs border-collapse">
            <thead>
              <tr>
                <th className="text-left pr-2 sticky left-0 bg-white/95 font-body text-muted font-medium">
                  Zona / Hora
                </th>
                {data.hours.map((h) => (
                  <th key={h} className="px-1 py-1 font-body text-muted font-normal">
                    {String(h).padStart(2, '0')}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.zoneNames.map((zname, zi) => (
                <tr key={zname}>
                  <td className="pr-2 py-1 font-body font-semibold text-ink sticky left-0 bg-white/95 whitespace-nowrap">
                    {zname}
                  </td>
                  {data.hours.map((h, hi) => {
                    const excess = data.exceedanceMatrix[zi][hi];
                    const laeq = data.laeqMatrix[zi][hi];
                    return (
                      <td
                        key={h}
                        className={`w-10 h-8 text-center rounded border border-white/50 ${colorFor(excess)}`}
                        title={
                          excess === -1
                            ? 'Sin datos'
                            : `${zname} · ${h}h\nLAeq: ${laeq} dB\nExceso: ${excess > 0 ? '+' : ''}${excess} dB`
                        }
                      >
                        {excess === -1 ? '' : laeq.toFixed(0)}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Card>
  );
}
