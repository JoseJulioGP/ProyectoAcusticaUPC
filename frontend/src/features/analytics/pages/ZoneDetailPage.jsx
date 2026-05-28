import { useParams, Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { analyticsApi } from "../api/analyticsApi";

export default function ZoneDetailPage() {
  const { zoneId } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    analyticsApi
      .zoneDetail(zoneId, { from: undefined, to: undefined })
      .then((r) => setData(r))
      .finally(() => setLoading(false));
  }, [zoneId]);

  if (loading) return <div className="p-6">Cargando...</div>;
  if (!data) return <div className="p-6">No se encontró la zona</div>;

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <Link to="/dashboard" className="text-sm text-blue-600 hover:underline">
        ← Volver al dashboard
      </Link>
      <h1 className="text-2xl font-bold text-slate-800 mt-2">{data.zoneName}</h1>
      <p className="text-sm text-slate-500 mb-6">
        Sector {data.sector} — {data.subsector}. {data.description}
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div className="bg-white border border-slate-200 rounded p-4">
          <h3 className="text-sm font-semibold mb-2">Resumen estadístico</h3>
          <ul className="text-sm space-y-1">
            <li>Mediciones: {data.stats.measurements.toLocaleString("es-CO")}</li>
            <li>LAeq diurno: {data.stats.laeqDiurnoDb ?? "—"} dB (estándar {data.stats.standardDayDb} dB)</li>
            <li>LAeq nocturno: {data.stats.laeqNocturnoDb ?? "—"} dB (estándar {data.stats.standardNightDb} dB)</li>
            <li>Alertas: {data.stats.alertsCount}</li>
            <li>Estado: <strong>{data.stats.overallStatus}</strong></li>
          </ul>
        </div>
        <div className="bg-white border border-slate-200 rounded p-4">
          <h3 className="text-sm font-semibold mb-2">Batches recientes</h3>
          {data.recentBatches.length === 0 ? (
            <p className="text-sm text-slate-500">Sin batches recientes</p>
          ) : (
            <ul className="text-sm space-y-1">
              {data.recentBatches.map((b) => (
                <li key={b.id}>
                  <Link to={`/ingest/${b.id}`} className="text-blue-600 hover:underline">
                    {b.fileName}
                  </Link>{" "}
                  · {b.validRows} filas · {new Date(b.uploadedAt).toLocaleDateString("es-CO")}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="bg-white border border-slate-200 rounded p-4">
        <h3 className="text-sm font-semibold mb-2">Alertas recientes</h3>
        {data.recentAlerts.length === 0 ? (
          <p className="text-sm text-slate-500">Sin alertas recientes</p>
        ) : (
          <ul className="text-sm space-y-1">
            {data.recentAlerts.map((a) => (
              <li key={a.id}>
                <span
                  className={`inline-block px-2 py-0.5 rounded text-xs mr-2 ${
                    a.severity === "CRITICA"
                      ? "bg-red-100 text-red-700"
                      : a.severity === "MODERADA"
                      ? "bg-orange-100 text-orange-700"
                      : "bg-yellow-100 text-yellow-700"
                  }`}
                >
                  {a.severity}
                </span>
                {" "}
                {`${a.measuredDb} dB (límite ${a.standardDb} dB, exceso +${a.excessDb} dB)`} · {new Date(a.triggeredAt).toLocaleString("es-CO")}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}