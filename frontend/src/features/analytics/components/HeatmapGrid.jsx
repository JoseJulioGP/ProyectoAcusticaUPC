import { useHeatmap } from "../hooks/useHeatmap";

function colorFor(excess) {
  if (excess === -1 || excess == null) return "bg-slate-100 text-slate-400";
  if (excess <= 0) return "bg-green-200 text-green-900";
  if (excess <= 3) return "bg-yellow-200 text-yellow-900";
  if (excess <= 8) return "bg-orange-300 text-orange-900";
  return "bg-red-400 text-red-50";
}

export default function HeatmapGrid() {
  const { data, loading, error } = useHeatmap();

  if (loading) return <div className="h-72 bg-slate-100 animate-pulse rounded"></div>;
  if (error) return <div className="text-red-600">Error al cargar el mapa</div>;
  if (!data || data.zoneNames.length === 0)
    return (
      <div className="h-72 flex items-center justify-center text-slate-500">
        Sin datos para el mapa de calor
      </div>
    );

  const { zoneNames, hours, laeqMatrix, exceedanceMatrix } = data;

  return (
    <div className="bg-white rounded-lg border border-slate-200 p-4 overflow-x-auto">
      <h3 className="text-sm font-semibold text-slate-700 mb-2">
        Mapa de calor — LAeq por zona y hora del día
      </h3>
      <p className="text-xs text-slate-500 mb-3">
        Colores reflejan el exceso vs estándar.
        <span className="inline-block ml-2 w-3 h-3 align-middle bg-green-200"></span> Cumple
        <span className="inline-block ml-2 w-3 h-3 align-middle bg-yellow-200"></span> ≤3 dB
        <span className="inline-block ml-2 w-3 h-3 align-middle bg-orange-300"></span> 3-8 dB
        <span className="inline-block ml-2 w-3 h-3 align-middle bg-red-400"></span> &gt;8 dB
      </p>
      <table className="text-xs border-collapse">
        <thead>
          <tr>
            <th className="text-left pr-2 sticky left-0 bg-white">Zona / Hora</th>
            {hours.map((h) => (
              <th key={h} className="px-1 py-1 text-slate-500 font-normal">
                {String(h).padStart(2, "0")}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {zoneNames.map((zname, zi) => (
            <tr key={zname}>
              <td className="pr-2 py-1 font-medium text-slate-700 sticky left-0 bg-white whitespace-nowrap">
                {zname}
              </td>
              {hours.map((h, hi) => {
                const excess = exceedanceMatrix[zi][hi];
                const laeq = laeqMatrix[zi][hi];
                return (
                  <td
                    key={h}
                    className={`w-10 h-8 text-center border border-white ${colorFor(excess)}`}
                    title={
                      excess === -1
                        ? "Sin datos"
                        : `${zname} · ${h}h\nLAeq: ${laeq} dB\nExceso: ${excess > 0 ? "+" : ""}${excess} dB`
                    }
                  >
                    {excess === -1 ? "" : laeq.toFixed(0)}
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