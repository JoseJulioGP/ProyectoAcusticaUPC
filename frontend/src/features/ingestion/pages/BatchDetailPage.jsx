import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { ingestionApi } from "../api/ingestionApi";
import BatchStatusBadge from "../components/BatchStatusBadge";
import MeasurementsTable from "../components/MeasurementsTable";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useComplianceResult } from "../../compliance/hooks/useComplianceResult";
import { complianceApi } from "../../compliance/api/complianceApi";
import ComplianceResultCard from "../../compliance/components/ComplianceResultCard";
import { useRole } from "../../auth/hooks/useRole";
import BatchObservationCard from "../components/BatchObservationCard";

export default function BatchDetailPage() {
  const { batchId } = useParams();
  const [batch, setBatch] = useState(null);
  const [page, setPage] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0, last: true });
  const [pageNum, setPageNum] = useState(0);
  const [loading, setLoading] = useState(true);

  const {
    results: complianceResults,
    loading: cLoading,
    refresh: refreshCompliance,
  } = useComplianceResult(batchId);
  const { isAdmin, isAnalyst } = useRole();
  const canManage = isAdmin || isAnalyst;

  useEffect(() => {
    ingestionApi.get(batchId).then(setBatch);
  }, [batchId]);

  useEffect(() => {
    setLoading(true);
    ingestionApi
      .measurements(batchId, { page: pageNum, size: 50 })
      .then(setPage)
      .finally(() => setLoading(false));
  }, [batchId, pageNum]);

  async function handleEvaluate() {
    await complianceApi.evaluateBatch(batchId);

    let attempts = 0;

    const interval = setInterval(async () => {
      attempts += 1;
      const res = await complianceApi.getByBatch(batchId);

      if (res.length > 0 || attempts >= 15) {
        refreshCompliance();
        clearInterval(interval);
      }
    }, 2000);
  }

  if (!batch) return <div className="text-slate-500">Cargando batch…</div>;

  return (
    <div className="acu-stagger space-y-6">
      <Link to="/ingest" className="inline-flex items-center text-sm text-indigo-600 hover:text-indigo-500">
        <ChevronLeft className="h-4 w-4 mr-1" /> Volver a ingesta
      </Link>

      <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-xl font-bold text-slate-800">{batch.fileName}</h1>
            <p className="mt-1 text-sm text-slate-600">{batch.zoneName}</p>
          </div>
          <BatchStatusBadge status={batch.status} />
        </div>
        <dl className="mt-4 grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
          <div>
            <dt className="text-slate-500">Filas totales</dt>
            <dd className="font-semibold text-slate-800 tabular-nums">{batch.totalRows?.toLocaleString("es-CO") ?? "—"}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Válidas</dt>
            <dd className="font-semibold text-emerald-700 tabular-nums">{batch.validRows?.toLocaleString("es-CO") ?? "—"}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Omitidas</dt>
            <dd className="font-semibold text-amber-700 tabular-nums">{batch.rejectedRows ?? 0}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Procesado</dt>
            <dd className="text-slate-700">{batch.processedAt ? new Date(batch.processedAt).toLocaleString("es-CO") : "—"}</dd>
          </div>
        </dl>
        {batch.errorMessage && (
          <div className="mt-3 rounded-md bg-rose-50 px-3 py-2 text-sm text-rose-700 ring-1 ring-rose-200">
            {batch.errorMessage}
          </div>
        )}

        <section className="mt-6">
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-semibold">Cumplimiento Res. 627</h2>

            {isAdmin && complianceResults.length === 0 && !cLoading && (
              <button
                onClick={handleEvaluate}
                className="bg-blue-600 hover:bg-blue-700 text-white text-sm px-3 py-1.5 rounded"
              >
                Calcular cumplimiento
              </button>
            )}
          </div>

          {cLoading && <div className="text-gray-400 text-sm">Cargando...</div>}

          {complianceResults.length === 0 && !cLoading && (
            <p className="text-gray-500 text-sm">
              Aun no se ha evaluado el cumplimiento de este batch.
              {isAdmin ? "" : " Pidele a un administrador que lo dispare."}
            </p>
          )}

          <div className="grid gap-3">
            {complianceResults.map((r) => (
              <ComplianceResultCard key={r.id} result={r} />
            ))}
          </div>
        </section>
      </div>

      <BatchObservationCard
        batchId={batchId}
        value={batch.observation ?? ""}
        updatedAt={batch.observationUpdatedAt}
        canEdit={canManage}
      />

      <div>
        <h2 className="text-lg font-semibold text-slate-800 mb-3">
          Mediciones {page.totalElements > 0 && <span className="text-slate-500">({page.totalElements.toLocaleString("es-CO")})</span>}
        </h2>
        {loading ? (
          <div className="text-sm text-slate-500">Cargando…</div>
        ) : (
          <>
            <MeasurementsTable measurements={page.content} />
            {page.totalPages > 1 && (
              <div className="mt-4 flex items-center justify-between text-sm">
                <span className="text-slate-600">
                  Página {page.page + 1} de {page.totalPages}
                </span>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPageNum((p) => Math.max(0, p - 1))}
                    disabled={page.page === 0}
                    className="inline-flex items-center rounded-md border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    <ChevronLeft className="h-4 w-4 mr-1" /> Anterior
                  </button>
                  <button
                    onClick={() => setPageNum((p) => p + 1)}
                    disabled={page.last}
                    className="inline-flex items-center rounded-md border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    Siguiente <ChevronRight className="h-4 w-4 ml-1" />
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}