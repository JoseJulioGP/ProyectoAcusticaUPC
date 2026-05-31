import UploadDropZone from "../components/UploadDropZone";
import BatchListTable from "../components/BatchListTable";
import { useIngesta } from "../hooks/useIngesta";

export default function IngestionPage() {
  const { rows, loading, isAdmin, retry, markFailed, remove, reload } = useIngesta();

  return (
    <div className="space-y-6">
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
            Historial {rows.length > 0 && <span className="text-slate-500">({rows.length})</span>}
          </h2>
          {loading && <span className="text-xs text-slate-500">Actualizando…</span>}
        </div>
        <BatchListTable
          batches={rows}
          isAdmin={isAdmin}
          onRetry={retry}
          onMarkFailed={markFailed}
          onRemove={remove}
        />
      </div>
    </div>
  );
}
