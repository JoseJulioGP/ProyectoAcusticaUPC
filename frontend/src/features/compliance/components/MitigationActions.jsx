import { useEffect, useState } from "react";
import { mitigationsApi } from "../api/mitigationsApi";

/**
 * Lista de acciones correctivas / alternativas de mitigación para un exceso dado.
 * Consume GET /mitigations/suggest?excessDb=X (ordenado por prioridad).
 */
export default function MitigationActions({ excessDb }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(false);
    mitigationsApi
      .suggest(excessDb)
      .then((d) => !cancelled && setItems(Array.isArray(d) ? d : []))
      .catch(() => !cancelled && setError(true))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [excessDb]);

  if (loading) return <p className="text-sm text-slate-400">Cargando sugerencias…</p>;
  if (error) return <p className="text-sm text-rose-600">No se pudieron cargar las acciones.</p>;
  if (items.length === 0)
    return <p className="text-sm text-slate-500">No hay acciones sugeridas para este nivel de exceso.</p>;

  return (
    <ol className="space-y-2">
      {items.map((m) => (
        <li key={m.id} className="rounded-lg border border-slate-200 p-3">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-sm font-semibold text-slate-800">{m.title}</p>
              {m.description && <p className="text-sm text-slate-600 mt-0.5">{m.description}</p>}
            </div>
            {m.priority != null && (
              <span className="shrink-0 rounded-full bg-petroleo/10 px-2 py-0.5 text-xs font-semibold text-petroleo">
                P{m.priority}
              </span>
            )}
          </div>
          <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500">
            {m.regulationRef && <span>Ref.: {m.regulationRef}</span>}
            {m.estimatedImpactDb != null && (
              <span>Impacto estimado: −{Number(m.estimatedImpactDb).toFixed(1)} dB</span>
            )}
          </div>
        </li>
      ))}
    </ol>
  );
}
