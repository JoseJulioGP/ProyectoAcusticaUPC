const formatTime = (iso) => {
  return new Date(iso).toLocaleString("es-CO", {
    dateStyle: "short",
    timeStyle: "medium",
  });
};

const PERIOD_STYLES = {
  DIURNO: "bg-amber-50 text-amber-700 ring-amber-200",
  NOCTURNO: "bg-indigo-50 text-indigo-700 ring-indigo-200",
};

export default function MeasurementsTable({ measurements }) {
  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-4 py-2 text-left text-xs font-semibold text-slate-600 uppercase">Fecha y hora</th>
            <th className="px-4 py-2 text-right text-xs font-semibold text-slate-600 uppercase">Valor</th>
            <th className="px-4 py-2 text-left text-xs font-semibold text-slate-600 uppercase">Unidad</th>
            <th className="px-4 py-2 text-left text-xs font-semibold text-slate-600 uppercase">Periodo</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {measurements.map((m) => (
            <tr key={m.id} className="hover:bg-slate-50">
              <td className="px-4 py-2 text-sm text-slate-700 tabular-nums">{formatTime(m.measuredAt)}</td>
              <td className="px-4 py-2 text-right text-sm font-medium tabular-nums">{m.dbValue?.toFixed(1)}</td>
              <td className="px-4 py-2 text-sm text-slate-600">{m.unit}</td>
              <td className="px-4 py-2 text-sm">
                <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset ${PERIOD_STYLES[m.period] ?? ""}`}>
                  {m.period}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}