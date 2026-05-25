import { useEffect, useState } from "react";
import { ingestionApi } from "../api/ingestionApi";

const POLL_INTERVAL_MS = 3000;
const TERMINAL_STATUSES = ["COMPLETED", "FAILED"];

export function useBatchPolling(batchId) {
  const [batch, setBatch] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!batchId) return;

    let cancelled = false;
    let timer = null;

    const tick = async () => {
      try {
        const data = await ingestionApi.get(batchId);
        if (cancelled) return;
        setBatch(data);
        if (!TERMINAL_STATUSES.includes(data.status)) {
          timer = setTimeout(tick, POLL_INTERVAL_MS);
        }
      } catch (err) {
        if (!cancelled) setError(err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    setLoading(true);
    tick();

    return () => {
      cancelled = true;
      if (timer) clearTimeout(timer);
    };
  }, [batchId]);

  return { batch, loading, error };
}
