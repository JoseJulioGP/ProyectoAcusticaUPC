import { useState } from "react";
import { ingestionApi } from "../api/ingestionApi";
import { useToast } from "../../../shared/ui/useToast";

const formatDate = (iso) =>
  iso ? new Date(iso).toLocaleString("es-CO", { dateStyle: "short", timeStyle: "short" }) : null;

/**
 * Sección "Observaciones" del batch. Editable para ADMIN/ANALYST; VIEWER en read-only.
 */
export default function BatchObservationCard({ batchId, value = "", updatedAt, canEdit }) {
  const toast = useToast();
  const [text, setText] = useState(value ?? "");
  const [saved, setSaved] = useState(value ?? "");
  const [savedAt, setSavedAt] = useState(updatedAt ?? null);
  const [saving, setSaving] = useState(false);

  const dirty = text !== saved;

  const handleSave = async () => {
    setSaving(true);
    try {
      const res = await ingestionApi.updateObservation(batchId, text.trim());
      setSaved(text);
      setSavedAt(res?.observationUpdatedAt ?? new Date().toISOString());
      toast.success("Observación guardada.");
    } catch (err) {
      toast.error(err.response?.data?.message || "No se pudo guardar la observación.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-lg font-semibold text-slate-800">Observaciones</h2>
        {savedAt && (
          <span className="text-xs text-slate-500">Última actualización: {formatDate(savedAt)}</span>
        )}
      </div>

      {canEdit ? (
        <>
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            rows={4}
            placeholder="Añade notas u observaciones sobre esta carga…"
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 focus:border-petroleo focus:outline-none focus:ring-2 focus:ring-petroleo/20"
          />
          <div className="mt-3 flex justify-end">
            <button
              type="button"
              onClick={handleSave}
              disabled={saving || !dirty}
              className="rounded-[11px] bg-petroleo px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-petroleo/90 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {saving ? "Guardando…" : "Guardar"}
            </button>
          </div>
        </>
      ) : (
        <p className="text-sm text-slate-700 whitespace-pre-wrap">
          {saved?.trim() ? saved : <span className="text-slate-400">Sin observaciones.</span>}
        </p>
      )}
    </section>
  );
}
