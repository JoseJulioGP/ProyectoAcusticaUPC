import { useState } from "react";
import { Loader2 } from "lucide-react";
import Modal from "../../../shared/ui/Modal";
import PasswordStrength from "./PasswordStrength";
import { isPasswordValid } from "../domain/password";
import { authApi } from "../api/authApi";
import { useToast } from "../../../shared/ui/useToast";

const EMPTY = { identifier: "", current: "", next: "", confirm: "" };

/**
 * Modal de cambio de contraseña. Pide usuario/correo + contraseña actual +
 * nueva. El flujo valida la actual haciendo login y luego cambia la contraseña
 * con ese token (ver authApi). Funciona igual desde el LOGIN (sin sesión) o
 * estando autenticado en cualquier rol.
 *
 * Props opcionales:
 *  - defaultIdentifier: precarga el campo usuario/correo (útil con sesión).
 *  - onSuccess: callback tras cambiar la contraseña (p. ej. cerrar sesión).
 */
export default function ChangePasswordModal({ open, onClose, defaultIdentifier = "", onSuccess }) {
  const toast = useToast();
  const initial = () => ({ ...EMPTY, identifier: defaultIdentifier });
  const [form, setForm] = useState(initial);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const close = () => {
    setForm(initial());
    setError(null);
    onClose?.();
  };

  const nextValid = isPasswordValid(form.next);
  const confirmMatches = form.next.length > 0 && form.next === form.confirm;
  const canSubmit =
    form.identifier.trim().length > 0 &&
    form.current.length > 0 &&
    nextValid &&
    confirmMatches &&
    !busy;

  const onChange = (field) => (e) =>
    setForm((f) => ({ ...f, [field]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!canSubmit) return;
    setBusy(true);
    setError(null);
    try {
      await authApi.changePasswordWithCredentials({
        identifier: form.identifier.trim(),
        currentPassword: form.current,
        newPassword: form.next,
      });
      toast.success("Contraseña actualizada. Inicia sesión con la nueva.");
      close();
      onSuccess?.();
    } catch (err) {
      const status = err.response?.status;
      const code = err.response?.data?.code;
      if (status === 401 || status === 403 || code === "CURRENT_PASSWORD_INVALID") {
        setError("Usuario o contraseña actual incorrectos.");
      } else if (status === 400) {
        setError(
          err.response?.data?.message ||
            "La nueva contraseña no cumple los requisitos."
        );
      } else {
        setError(
          err.response?.data?.message || "No se pudo cambiar la contraseña."
        );
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={close}
      title="Cambiar contraseña"
      width={460}
      footer={
        <>
          <button
            type="button"
            onClick={close}
            className="rounded-[11px] border-[1.5px] border-petroleo/15 px-4 py-2 text-sm font-semibold text-petroleo hover:bg-petroleo/[0.06]"
          >
            Cancelar
          </button>
          <button
            type="submit"
            form="change-password-form"
            disabled={!canSubmit}
            className="inline-flex items-center gap-2 rounded-[11px] bg-petroleo px-4 py-2 text-sm font-semibold text-white hover:bg-petroleo/90 disabled:opacity-50"
          >
            {busy && <Loader2 size={15} className="animate-spin" />}
            {busy ? "Guardando…" : "Guardar"}
          </button>
        </>
      }
    >
      <form id="change-password-form" onSubmit={onSubmit} className="space-y-4">
        <Field
          label="Usuario o correo"
          type="text"
          value={form.identifier}
          onChange={onChange("identifier")}
          autoComplete="username"
          autoFocus
        />
        <Field
          label="Contraseña actual"
          value={form.current}
          onChange={onChange("current")}
          autoComplete="current-password"
        />

        <div>
          <Field
            label="Nueva contraseña"
            value={form.next}
            onChange={onChange("next")}
            autoComplete="new-password"
          />
          <PasswordStrength value={form.next} />
        </div>

        <div>
          <Field
            label="Confirmar nueva contraseña"
            value={form.confirm}
            onChange={onChange("confirm")}
            autoComplete="new-password"
          />
          {form.confirm.length > 0 && !confirmMatches && (
            <p className="mt-1 text-xs text-rose-600">Las contraseñas no coinciden.</p>
          )}
        </div>

        {error && (
          <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>
        )}
      </form>
    </Modal>
  );
}

function Field({ label, value, onChange, type = "password", autoComplete, autoFocus = false }) {
  return (
    <label className="block">
      <span className="mb-1 block text-xs font-semibold uppercase tracking-wider text-slate-500">
        {label}
      </span>
      <input
        type={type}
        value={value}
        onChange={onChange}
        autoFocus={autoFocus}
        autoComplete={autoComplete}
        className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-800 outline-none focus:border-petroleo focus:ring-1 focus:ring-petroleo"
      />
    </label>
  );
}
