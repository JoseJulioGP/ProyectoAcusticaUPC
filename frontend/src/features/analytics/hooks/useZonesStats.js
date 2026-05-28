import { useEffect, useState } from "react";
import { analyticsApi } from "../api/analyticsApi";
import { useDashboardFilter } from "../context/DashboardFilterContext";

export function useZonesStats() {
  const { from, to } = useDashboardFilter();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    analyticsApi
      .zonesStats({ from, to })
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
  }, [from, to]);

  return { data, loading, error };
}