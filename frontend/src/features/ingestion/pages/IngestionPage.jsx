import { useMemo, useState } from "react";
import { MapPin } from "lucide-react";
import UploadDropZone from "../components/UploadDropZone";
import BatchListTable from "../components/BatchListTable";
import { useIngesta } from "../hooks/useIngesta";

const ACTIVE = ["PENDING", "PROCESSING"];

export default function IngestionPage() {
  const { rows, loading, isAdmin, retry, markFailed, remove, reload } = useIngesta();
  const [selectedKey, setSelectedKey] = useState(null);

  // Agrupa el historial por zona.
  const groups = useMemo(() => {
    const map = new Map();
    for (const b of rows) {
      const key = b.zoneId ?? b.zoneName ?? "__none__";
      if (!map.has(key)) {
        map.set(key, { key, name: b.zoneName ?? "Sin zona asignada", batches: [] });
      }
      map.get(key).batches.push(b);
    }
    return [...map.values()].sort((a, b) => a.name.localeCompare(b.name, "es"));
  }, [rows]);

  // Zona activa (la seleccionada, o la primera por defecto).
  const activeGroup = groups.find((g) => g.key === selectedKey) ?? groups[0] ?? null;

  const totalMediciones = rows.reduce((s, b) => s + (b.validRows ?? 0), 0);

  return (
    <div className="acu-stagger space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Ingesta de mediciones</h1>
        <p className="mt-1 text-sm text-slate-600">
          Sube archivos exportados del sonómetro. El procesamiento se ejecuta en segundo plano.
        </p>
      </div>

      <UploadDropZone onUploadSuccess={reload} />

      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-semibold text-slate-800">
            Historial por zona {rows.length > 0 && <span className="text-slate-500">({rows.length})</span>}
          </h2>
          {loading && <span className="text-xs text-slate-500">Actualizando…</span>}
        </div>

        {rows.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
            Aún no hay cargas. Sube tu primer archivo arriba.
          </div>
        ) : (
          <>
            {/* Grilla de tarjetas por zona (clic para ver archivos) */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 mb-5">
              {groups.map((g) => {
                const completed = g.batches.filter((b) => b.status === "COMPLETED").length;
                const processing = g.batches.filter((b) => ACTIVE.includes(b.status)).length;
                const failed = g.batches.filter((b) => b.status === "FAILED").length;
                const mediciones = g.batches.reduce((s, b) => s + (b.validRows ?? 0), 0);
                const selected = activeGroup?.key === g.key;
                return (
                  <button
                    key={g.key}
                    type="button"
                    onClick={() => setSelectedKey(g.key)}
                    className={`acu-row text-left block p-3 border rounded-xl bg-white ${
                      selected ? "border-petroleo ring-1 ring-petroleo/30" : "border-slate-200"
                    }`}
                  >
                    <div className="flex justify-between items-start gap-2">
                      <p className="font-medium text-slate-800 flex items-center gap-1.5">
                        <MapPin size={14} className="text-petroleo shrink-0" />
                        {g.name}
                      </p>
                      <span className="shrink-0 text-xs font-semibold text-slate-500">
                        {g.batches.length} archivo(s)
                      </span>
                    </div>
                    <div className="mt-2 text-xs flex flex-wrap gap-x-3 gap-y-1">
                      {completed > 0 && <span className="text-emerald-700">{completed} completados</span>}
                      {processing > 0 && <span className="text-amber-600">{processing} en proceso</span>}
                      {failed > 0 && <span className="text-rose-600">{failed} fallidos</span>}
                    </div>
                    <p className="text-xs text-slate-500 mt-1">
                      {mediciones.toLocaleString("es-CO")} mediciones
                    </p>
                  </button>
                );
              })}
            </div>

            {/* Archivos de la zona seleccionada */}
            {activeGroup && (
              <div>
                <div className="flex items-center gap-2 mb-2 px-1">
                  <MapPin size={16} className="text-petroleo shrink-0" />
                  <h3 className="font-semibold text-slate-800">{activeGroup.name}</h3>
                  <span className="text-xs text-slate-500">· {activeGroup.batches.length} archivo(s)</span>
                </div>
                <BatchListTable
                  batches={activeGroup.batches}
                  isAdmin={isAdmin}
                  hideZone
                  onRetry={retry}
                  onMarkFailed={markFailed}
                  onRemove={remove}
                />
              </div>
            )}

            {/* Total general */}
            <div className="mt-5 rounded-xl border border-slate-200 bg-white px-4 py-3 flex flex-wrap items-center justify-between gap-2">
              <span className="text-sm text-slate-600">
                {groups.length} zona(s) · {rows.length} archivo(s)
              </span>
              <span className="text-sm font-semibold text-slate-800">
                Total: {totalMediciones.toLocaleString("es-CO")} mediciones
              </span>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
