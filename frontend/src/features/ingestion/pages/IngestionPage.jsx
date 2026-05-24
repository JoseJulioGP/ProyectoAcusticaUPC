import UploadDropZone from "../components/UploadDropZone";
import BatchListTable from "../components/BatchListTable";
import { useBatchListPolling } from "../hooks/useBatchListPolling";

export default function IngestionPage() {
  const { data, loading, refresh } = useBatchListPolling();

  const handleUploadSuccess = () => {
    refresh();
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Ingesta de mediciones</h1>
        <p className="mt-1 text-sm text-slate-600">
          Sube archivos exportados del sonómetro. El procesamiento se ejecuta en segundo plano.
        </p>
      </div>

      <UploadDropZone onUploadSuccess={handleUploadSuccess} />

      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-semibold text-slate-800">
            Historial {data.totalElements > 0 && <span className="text-slate-500">({data.totalElements})</span>}
          </h2>
          {loading && <span className="text-xs text-slate-500">Actualizando…</span>}
        </div>
        <BatchListTable batches={data.content} />
      </div>
    </div>
  );
}