import { Fragment, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ChevronDown } from "lucide-react";
import { complianceApi } from "../api/complianceApi";
import { zonesApi } from "../../zones/api/zonesApi";
import AlertSeverityBadge from "../components/AlertSeverityBadge";
import MitigationActions from "../components/MitigationActions";
import BatchObservationNote from "../../ingestion/components/BatchObservationNote";

const SEVERITIES = ["LEVE", "MODERADA", "CRITICA"];
const PERIOD_LABEL = { DIURNO: "Diurno", NOCTURNO: "Nocturno" };
const PAGE_SIZE = 20;
// /compliance/alerts devuelve Page<AlertDTO> de Spring (page index = `number`).
const EMPTY_PAGE = { content: [], number: 0, size: PAGE_SIZE, totalElements: 0, totalPages: 0, last: true };

const fmt = (iso) => (iso ? new Date(iso).toLocaleString("es-CO", { dateStyle: "short", timeStyle: "short" }) : "—");

function defaultFilters(zoneId = "") {
  const to = new Date();
  const from = new Date(to.getTime() - 30 * 24 * 60 * 60 * 1000);
  return {
    zoneId,
    from: from.toISOString().slice(0, 10),
    to: to.toISOString().slice(0, 10),
    severity: "",
  };
}

export default function AlertsPage() {
  const [searchParams] = useSearchParams();
  const [filters, setFilters] = useState(() => defaultFilters(searchParams.get("zoneId") ?? ""));
  const [page, setPage] = useState(0);
  const [data, setData] = useState(EMPTY_PAGE);
  const [loading, setLoading] = useState(false);
  const [zones, setZones] = useState([]);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    zonesApi.list().then(setZones).catch(() => setZones([]));
  }, []);

  useEffect(() => {
    setLoading(true);
    complianceApi
      .listAlerts({
        zoneId: filters.zoneId || undefined,
        from: filters.from + "T00:00:00-05:00",
        to: filters.to + "T23:59:59-05:00",
        page,
        size: PAGE_SIZE,
      })
      .then((res) => setData(res ?? EMPTY_PAGE))
      .catch(() => setData(EMPTY_PAGE))
      .finally(() => setLoading(false));
  }, [filters, page]);

  const update = (patch) => {
    setFilters((f) => ({ ...f, ...patch }));
    setPage(0);
  };

  // El endpoint no filtra por severidad → se filtra la página actual en cliente.
  const visible = useMemo(() => {
    const content = data.content ?? [];
    return filters.severity ? content.filter((a) => a.severity === filters.severity) : content;
  }, [data, filters.severity]);

  return (
    <div className="acu-stagger">
      <h1 className="text-2xl font-bold mb-2">Alertas</h1>
      <p className="text-gray-600 mb-4">
        Alertas generadas cuando una zona supera el límite de la Resolución 0627 de 2006.
        Haz clic en una fila para ver el detalle y las acciones recomendadas.
      </p>

      {/* Resumen de alertas */}
      <div className="flex flex-wrap gap-3 mb-4">
        <div className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 min-w-[140px]">
          <p className="text-xs text-slate-500">Alertas en el rango</p>
          <p className="text-xl font-semibold text-slate-800">
            {data.totalElements.toLocaleString("es-CO")}
          </p>
        </div>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap gap-3 items-end mb-4">
        <div>
          <label className="block text-xs text-gray-500">Zona</label>
          <select className="border rounded px-2 py-1 text-sm" value={filters.zoneId}
            onChange={(e) => update({ zoneId: e.target.value })}>
            <option value="">Todas</option>
            {zones.map((z) => <option key={z.id} value={z.id}>{z.name}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs text-gray-500">Desde</label>
          <input type="date" className="border rounded px-2 py-1 text-sm"
            value={filters.from} onChange={(e) => update({ from: e.target.value })} />
        </div>
        <div>
          <label className="block text-xs text-gray-500">Hasta</label>
          <input type="date" className="border rounded px-2 py-1 text-sm"
            value={filters.to} onChange={(e) => update({ to: e.target.value })} />
        </div>
        <div>
          <label className="block text-xs text-gray-500">Severidad</label>
          <select className="border rounded px-2 py-1 text-sm" value={filters.severity}
            onChange={(e) => update({ severity: e.target.value })}>
            <option value="">Todas</option>
            {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </div>

      {/* Tabla */}
      {loading ? (
        <div className="text-center py-8 text-gray-400">Cargando…</div>
      ) : visible.length === 0 ? (
        <div className="text-center py-8 text-gray-500">No hay alertas para los filtros seleccionados.</div>
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-gray-500 border-b bg-slate-50">
                <th className="py-2 px-3">Disparada</th>
                <th className="px-3">Zona</th>
                <th className="px-3">Periodo</th>
                <th className="px-3 text-right">Medido (dB)</th>
                <th className="px-3 text-right">Estándar (dB)</th>
                <th className="px-3 text-right">Exceso (dB)</th>
                <th className="px-3">Severidad</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((a) => {
                const open = selected?.id === a.id;
                return (
                  <Fragment key={a.id}>
                    <tr
                      onClick={() => setSelected((cur) => (cur?.id === a.id ? null : a))}
                      className={`acu-trow border-b last:border-0 cursor-pointer ${open ? "bg-slate-50" : ""}`}
                    >
                      <td className="py-2 px-3">{fmt(a.triggeredAt)}</td>
                      <td className="px-3">{a.zoneName}</td>
                      <td className="px-3">{PERIOD_LABEL[a.period] ?? a.period}</td>
                      <td className="px-3 text-right font-mono">{Number(a.measuredDb).toFixed(2)}</td>
                      <td className="px-3 text-right font-mono">{Number(a.standardDb).toFixed(2)}</td>
                      <td className="px-3 text-right font-mono font-semibold text-red-700">
                        +{Number(a.excessDb).toFixed(2)}
                      </td>
                      <td className="px-3">
                        <span className="inline-flex items-center gap-1.5">
                          <AlertSeverityBadge severity={a.severity} />
                          <ChevronDown
                            size={14}
                            className={`text-slate-400 transition-transform ${open ? "rotate-180" : ""}`}
                          />
                        </span>
                      </td>
                    </tr>
                    {open && (
                      <tr className="border-b last:border-0 bg-slate-50">
                        <td colSpan={7} className="px-3 pb-4 pt-1">
                          <div className="space-y-4">
                            <div>
                              <h3 className="font-semibold text-slate-800 mb-1.5">Observación de la carga</h3>
                              <BatchObservationNote batchId={a.batchId} />
                            </div>
                            <div>
                              <h3 className="font-semibold text-slate-800 mb-1.5">Acciones recomendadas</h3>
                              <MitigationActions excessDb={a.excessDb} />
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Paginación (server-side; la severidad filtra solo la página visible) */}
      {data.totalElements > 0 && (
        <div className="mt-3 flex items-center justify-between text-sm">
          <span className="text-gray-500">
            {data.totalElements} alerta(s) · Página {data.number + 1} de {data.totalPages}
            {filters.severity && " · severidad filtrada en esta página"}
          </span>
          <div className="flex gap-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={data.number === 0}
              className="rounded border border-slate-300 px-3 py-1.5 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Anterior
            </button>
            <button
              onClick={() => setPage((p) => p + 1)}
              disabled={data.last}
              className="rounded border border-slate-300 px-3 py-1.5 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Siguiente
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
