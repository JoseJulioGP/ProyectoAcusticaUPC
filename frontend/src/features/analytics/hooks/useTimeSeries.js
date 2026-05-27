import { useEffect, useState } from "react";
import { analyticsApi } from "../api/analyticsApi";
import { useDashboardFilter } from "../context/DashboardFilterContext";

export function useTimeSeries({ granularity = "HOUR" } = {}) {
  const { from, to, zoneId, period } = useDashboardFilter();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    analyticsApi
      .timeseries({ from, to, zoneId, period, granularity })
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
  }, [from, to, zoneId, period, granularity]);

  return { data, loading, error };
}