import { useCallback, useEffect, useState } from "react";
import { complianceApi } from "../api/complianceApi";

export function useComplianceResult(batchId) {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const refresh = useCallback(() => {
    if (!batchId) return;
    setLoading(true);
    setError(null);
    complianceApi
      .getByBatch(batchId)
      .then(setResults)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [batchId]);

  useEffect(() => { refresh(); }, [refresh]);

  return { results, loading, error, refresh };
}