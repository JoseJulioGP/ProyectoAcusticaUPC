import { useState } from "react";

export default function ResetPasswordModal({ open, user, onClose, onSubmit }) {
  const [pwd, setPwd] = useState("");
  const [err, setErr] = useState(null);
  if (!open) return null;

  const submit = async () => {
    if (pwd.length < 8) { setErr("Mínimo 8 caracteres."); return; }
    try { await onSubmit(pwd); setPwd(""); onClose(); }
    catch (e) { setErr(e?.response?.data?.message ?? "No se pudo cambiar la clave."); }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-sm rounded-lg bg-white p-6 shadow-xl">
        <h2 className="mb-1 text-lg font-semibold">Resetear contraseña</h2>
        <p className="mb-4 text-sm text-gray-500">{user?.email}</p>
        {err && <p className="mb-3 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{err}</p>}
        <input type="password" className="mb-4 w-full rounded border px-3 py-2"
               placeholder="Nueva contraseña" value={pwd} onChange={(e) => setPwd(e.target.value)} />
        <div className="flex justify-end gap-2">
          <button onClick={onClose} className="rounded px-4 py-2 text-gray-600 hover:bg-gray-100">Cancelar</button>
          <button onClick={submit} className="rounded bg-amber-600 px-4 py-2 text-white hover:bg-amber-700">Cambiar</button>
        </div>
      </div>
    </div>
  );
}
