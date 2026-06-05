import { useCallback, useEffect, useState } from "react";
import { zonesApi } from "../api/zonesApi";

export function useZones() {
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await zonesApi.list();
      setZones(Array.isArray(data) ? data : data.content ?? []);
    } catch (e) {
      setError(e?.response?.data?.message ?? "No se pudieron cargar las zonas.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { reload(); }, [reload]);

  return { zones, loading, error, reload };
}
