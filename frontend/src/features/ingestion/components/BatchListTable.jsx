import { Link } from "react-router-dom";
import BatchStatusBadge from "./BatchStatusBadge";

const formatDate = (iso) => {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("es-CO", {
    dateStyle: "short",
    timeStyle: "short",
  });
};

function ActionBtn({ danger, onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`text-xs font-medium ${
        danger
          ? "text-rose-600 hover:text-rose-500"
          : "text-indigo-600 hover:text-indigo-500"
      }`}
    >
      {children}
    </button>
  );
}

export default function BatchListTable({
  batches,
  isAdmin,
  hideZone = false,
  bare = false,
  onRetry,
  onMarkFailed,
  onRemove,
}) {
  if (!batches || batches.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
        Aún no hay cargas. Sube tu primer archivo arriba.
      </div>
    );
  }

  return (
    <div className={bare ? "overflow-x-auto" : "overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"}>
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">Archivo</th>
            {!hideZone && (
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">Zona</th>
            )}
            <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">Estado</th>
            <th className="px-4 py-3 text-right text-xs font-semibold text-slate-600 uppercase tracking-wider">Mediciones</th>
            <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">Subido</th>
            <th className="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {batches.map((b) => (
            <tr key={b.id} className="acu-trow">
              <td className="px-4 py-3 text-sm font-medium text-slate-900">{b.fileName}</td>
              {!hideZone && (
                <td className="px-4 py-3 text-sm text-slate-600">{b.zoneName ?? "—"}</td>
              )}
              <td className="px-4 py-3"><BatchStatusBadge status={b.status} /></td>
              <td className="px-4 py-3 text-right text-sm tabular-nums text-slate-700">
                {b.validRows?.toLocaleString("es-CO") ?? "—"}
                {b.rejectedRows > 0 && (
                  <span className="ml-1 text-xs text-amber-600">(+{b.rejectedRows} omitidas)</span>
                )}
              </td>
              <td className="px-4 py-3 text-sm text-slate-600">{formatDate(b.uploadedAt)}</td>
              <td className="px-4 py-3">
                <div className="flex items-center justify-end gap-3">
                  {b.status === "COMPLETED" && (
                    <Link
                      to={`/ingest/${b.id}`}
                      className="text-xs font-medium text-indigo-600 hover:text-indigo-500"
                    >
                      Ver detalle →
                    </Link>
                  )}
                  {isAdmin && b.status === "PROCESSING" && (
                    <>
                      <ActionBtn onClick={() => onRetry(b.id)}>Reintentar</ActionBtn>
                      <ActionBtn danger onClick={() => onMarkFailed(b.id)}>Marcar fallido</ActionBtn>
                    </>
                  )}
                  {isAdmin && b.status === "FAILED" && (
                    <span
                      className="text-xs italic text-slate-500"
                      title="Para reintentar una carga fallida, elimínala y vuelve a subir el archivo."
                    >
                      Eliminar y resubir
                    </span>
                  )}
                  {isAdmin && (
                    <ActionBtn danger onClick={() => onRemove(b.id)}>Eliminar</ActionBtn>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
