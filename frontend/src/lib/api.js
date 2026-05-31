const BASE = import.meta.env.VITE_API_URL ?? '/api/v1';
const token = () => localStorage.getItem('acu-token');

async function req(method, path, { params, body } = {}) {
  const qs = params ? '?' + new URLSearchParams(params) : '';
  const res = await fetch(`${BASE}${path}${qs}`, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token() ? { Authorization: `Bearer ${token()}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (res.status === 204) return null;
  if (!res.ok) throw new Error((await res.json().catch(() => ({}))).message || res.statusText);
  return res.headers.get('content-type')?.includes('json') ? res.json() : res.blob();
}

export const api = {
  get: (p, o) => req('GET', p, o),
  post: (p, body) => req('POST', p, { body }),
  del: (p) => req('DELETE', p),
  // descarga binaria (PDF)
  async blob(p, params) {
    const qs = params ? '?' + new URLSearchParams(params) : '';
    const res = await fetch(`${BASE}${p}${qs}`, { headers: token() ? { Authorization: `Bearer ${token()}` } : {} });
    if (!res.ok) throw new Error('No se pudo generar el PDF');
    return res.blob();
  },
};