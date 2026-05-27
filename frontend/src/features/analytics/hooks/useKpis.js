import { useEffect, useState } from "react";
import { analyticsApi } from "../api/analyticsApi";
import { useDashboardFilter } from "../context/DashboardFilterContext";

export function useKpis() {
  const { from, to, zoneId, period } = useDashboardFilter();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    analyticsApi
      .kpis({ from, to, zoneId, period })
      .then((res) => {
        if (!cancelled) setData(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [from, to, zoneId, period]);

  return { data, loading, error };
}