import { useEffect, useState } from "react";

const ROLES = ["ADMIN", "ANALYST", "VIEWER"];

export default function UserFormModal({ open, initial, onClose, onSubmit }) {
  const isEdit = Boolean(initial?.id);
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [role, setRole] = useState("VIEWER");
  const [active, setActive] = useState(true);
  const [password, setPassword] = useState("");
  const [err, setErr] = useState(null);

  useEffect(() => {
    if (open) {
      setFullName(initial?.fullName ?? "");
      setEmail(initial?.email ?? "");
      setRole(initial?.role ?? "VIEWER");
      setActive(initial?.active ?? true);
      setPassword("");
      setErr(null);
    }
  }, [open, initial]);

  if (!open) return null;

  const submit = async () => {
    setErr(null);
    try {
      if (isEdit) {
        await onSubmit({ fullName, role, active });
      } else {
        if (password.length < 8) { setErr("La contraseña debe tener al menos 8 caracteres."); return; }
        await onSubmit({ fullName, email, role, password });
      }
      onClose();
    } catch (e) {
      setErr(e?.response?.data?.message ?? "No se pudo guardar.");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <h2 className="mb-4 text-lg font-semibold">{isEdit ? "Editar usuario" : "Nuevo usuario"}</h2>
        {err && <p className="mb-3 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{err}</p>}

        <label className="mb-1 block text-sm text-gray-600">Nombre completo</label>
        <input className="mb-3 w-full rounded border px-3 py-2" value={fullName}
               onChange={(e) => setFullName(e.target.value)} />

        <label className="mb-1 block text-sm text-gray-600">Email</label>
        <input className="mb-3 w-full rounded border px-3 py-2 disabled:bg-gray-100" value={email}
               disabled={isEdit} onChange={(e) => setEmail(e.target.value)} />

        <label className="mb-1 block text-sm text-gray-600">Rol</label>
        <select className="mb-3 w-full rounded border px-3 py-2" value={role}
                onChange={(e) => setRole(e.target.value)}>
          {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
        </select>

        {isEdit && (
          <label className="mb-3 flex items-center gap-2 text-sm">
            <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} />
            Usuario activo
          </label>
        )}

        {!isEdit && (
          <>
            <label className="mb-1 block text-sm text-gray-600">Contraseña inicial</label>
            <input type="password" className="mb-3 w-full rounded border px-3 py-2" value={password}
                   onChange={(e) => setPassword(e.target.value)} />
          </>
        )}

        <div className="mt-2 flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-4 py-2 text-gray-600 hover:bg-gray-100">Cancelar</button>
          <button onClick={submit} className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700">Guardar</button>
        </div>
      </div>
    </div>
  );
}