import { useCallback, useEffect, useState } from "react";
import { usersApi } from "../api/usersApi";

export function useUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await usersApi.list();
      setUsers(page.content ?? []);
    } catch (e) {
      setError(e?.response?.data?.message ?? "No se pudieron cargar los usuarios.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { reload(); }, [reload]);

  return { users, loading, error, reload };
}