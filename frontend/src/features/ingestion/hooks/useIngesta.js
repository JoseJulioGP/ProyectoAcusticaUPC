import { useEffect, useState, useCallback } from 'react';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth';

const POLL_INTERVAL_MS = 3000;
const ACTIVE_STATUSES = ['PENDING', 'PROCESSING'];

export function useIngesta() {
  const { role } = useAuth();
  const isAdmin = role === 'ADMIN';
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.get('/ingest/batches');
      setRows(data.content ?? data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    const hasActive = rows.some((r) => ACTIVE_STATUSES.includes(r.status));
    if (!hasActive) return;
    const id = setInterval(load, POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [rows, load]);

  const retry = async (id) => {
    await api.post(`/ingest/batches/${id}/retry`);
    await load();
  };

  const markFailed = async (id) => {
    await api.post(`/ingest/batches/${id}/fail`, { reason: 'Marcado como fallido manualmente' });
    await load();
  };

  const remove = async (id) => {
    if (!confirm('¿Eliminar esta ingesta? Se borrarán sus mediciones y se recalculará el dashboard.')) return;
    await api.del(`/ingest/batches/${id}`);
    await load();
  };

  return { rows, loading, isAdmin, retry, markFailed, remove, reload: load };
}
