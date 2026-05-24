import { useEffect, useState, useCallback } from "react";
import { ingestionApi } from "../api/ingestionApi";

const POLL_INTERVAL_MS = 3000;
const ACTIVE_STATUSES = ["PENDING", "PROCESSING"];

export function useBatchListPolling() {
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const refresh = useCallback(async () => {
    try {
      const result = await ingestionApi.list();
      setData(result);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    setLoading(true);
    refresh();
  }, [refresh]);

  useEffect(() => {
    const hasActive = data.content?.some((b) => ACTIVE_STATUSES.includes(b.status));
    if (!hasActive) return;

    const id = setInterval(refresh, POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [data, refresh]);

  return { data, loading, error, refresh };
}
