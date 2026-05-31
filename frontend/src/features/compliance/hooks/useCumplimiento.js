import { useEffect, useState } from 'react';
import { api } from '@/lib/api';

const today = () => new Date().toISOString().slice(0, 10);
const monthAgo = () => {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return d.toISOString().slice(0, 10);
};

// Offset Colombia (-05:00, sin DST). El backend espera OffsetDateTime ISO-8601.
const toIsoStart = (d) => `${d}T00:00:00-05:00`;
const toIsoEnd   = (d) => `${d}T23:59:59-05:00`;

/**
 * Estado y carga de la vista Cumplimiento.
 * - Dispara al MONTAR y cuando cambia cualquier filtro (zoneId | from | to).
 *   Esto cierra el bug del Sprint 5 ("no carga hasta tocar la fecha").
 * - `rows`  → resultados por periodo (requiere zona especifica; el backend exige zoneId).
 * - `alerts` → alertas del rango (zona opcional).
 */
export function useCumplimiento() {
  const [filters, setFilters] = useState({
    zoneId: 'todas',
    from: monthAgo(),
    to: today(),
  });
  const [zones, setZones] = useState([]);
  const [data, setData] = useState({ rows: [], alerts: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let alive = true;
    api.get('/zones')
      .then((z) => { if (alive) setZones(Array.isArray(z) ? z : (z?.content ?? [])); })
      .catch(() => { if (alive) setZones([]); });
    return () => { alive = false; };
  }, []);

  useEffect(() => {
    let alive = true;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const from = toIsoStart(filters.from);
        const to   = toIsoEnd(filters.to);

        const alertsParams = { from, to };
        if (filters.zoneId !== 'todas') alertsParams.zoneId = filters.zoneId;
        const alertsPromise = api.get('/compliance/alerts', { params: alertsParams })
          .then((r) => r?.content ?? []);

        const rowsPromise = (filters.zoneId !== 'todas')
          ? api.get('/compliance/results', { params: { ...alertsParams, zoneId: filters.zoneId } })
              .then((r) => r?.content ?? [])
          : Promise.resolve([]);

        const [alerts, rows] = await Promise.all([alertsPromise, rowsPromise]);
        if (alive) setData({ rows, alerts });
      } catch (e) {
        if (alive) setError(e?.message ?? 'Error al cargar cumplimiento');
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [filters.zoneId, filters.from, filters.to]);

  return { filters, setFilters, zones, rows: data.rows, alerts: data.alerts, loading, error };
}
