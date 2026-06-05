import { useEffect, useState } from "react";
import { ingestionApi } from "../api/ingestionApi";

const fmt = (iso) =>
  iso ? new Date(iso).toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" }) : null;

/** Muestra (solo lectura) la observación registrada en una carga (batch). */
export default function BatchObservationNote({ batchId }) {
  const [batch, setBatch] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!batchId) return;
    let cancelled = false;
    setLoading(true);
    ingestionApi
      .get(batchId)
      .then((b) => !cancelled && setBatch(b))
      .catch(() => !cancelled && setBatch(null))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [batchId]);

  if (!batchId) return <p className="text-sm text-slate-500">Sin carga asociada.</p>;
  if (loading) return <p className="text-sm text-slate-400">Cargando observación…</p>;

  const obs = batch?.observation?.trim();
  return obs ? (
    <>
      <p className="text-sm text-slate-700 whitespace-pre-wrap">{obs}</p>
      {batch.observationUpdatedAt && (
        <p className="text-xs text-slate-400 mt-1">Última actualización: {fmt(batch.observationUpdatedAt)}</p>
      )}
    </>
  ) : (
    <p className="text-sm text-slate-500">Sin observaciones para esta carga.</p>
  );
}
