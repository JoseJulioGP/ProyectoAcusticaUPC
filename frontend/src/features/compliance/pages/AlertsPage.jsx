import { useEffect, useState } from "react";
import { complianceApi } from "../api/complianceApi";
import AlertsTable from "../components/AlertsTable";
import ComplianceFilters from "../components/ComplianceFilters";

function defaultFilters() {
  const to = new Date();
  const from = new Date(to.getTime() - 30 * 24 * 60 * 60 * 1000);
  return {
    zoneId: "",
    from: from.toISOString().slice(0, 10),
    to: to.toISOString().slice(0, 10),
  };
}

export default function AlertsPage() {
  const [filters, setFilters] = useState(defaultFilters);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    complianceApi
      .listAlerts({
        zoneId: filters.zoneId || undefined,
        from: filters.from + "T00:00:00-05:00",
        to:   filters.to   + "T23:59:59-05:00",
      })
      .then(setData)
      .finally(() => setLoading(false));
  }, [filters]);

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-2">Cumplimiento — Alertas</h1>
      <p className="text-gray-600 mb-4">
        Alertas generadas cuando una zona supera el limite de la Resolucion 0627 de 2006.
      </p>
      <ComplianceFilters value={filters} onChange={setFilters} />
      {loading
        ? <div className="text-center py-8 text-gray-400">Cargando...</div>
        : <AlertsTable alerts={data.content} />
      }
      {data.totalElements > 0 && (
        <p className="mt-3 text-xs text-gray-500">
          {data.totalElements} alerta(s) en el rango.
        </p>
      )}
    </div>
  );
}