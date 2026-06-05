import { useEffect, useState } from "react";
import { analyticsApi } from "../api/analyticsApi";
import { useDashboardFilter } from "../context/DashboardFilterContext";

export function useHeatmap(override = {}) {
  const { from: gFrom, to: gTo, period } = useDashboardFilter();
  // Si el calendario pasa un día concreto, ese rango pisa el filtro global.
  const from = override.from ?? gFrom;
  const to = override.to ?? gTo;
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    analyticsApi
      .heatmap({ from, to, period })
      .then((res) => {
        if (!cancelled) setData(res);
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
  }, [from, to, period]);

  return { data, loading, error };
}