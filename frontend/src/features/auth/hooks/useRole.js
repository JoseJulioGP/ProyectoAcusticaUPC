import { useAuth } from "../context/AuthContext";

/**
 * Helpers de rol derivados del usuario logueado.
 * La UI usa esto para mostrar/ocultar; la seguridad real vive en el backend.
 */
export function useRole() {
  const { user } = useAuth();
  const role = user?.role ?? null;
  return {
    role,
    isAdmin: role === "ADMIN",
    isAnalyst: role === "ANALYST",
    isViewer: role === "VIEWER",
    canIngest: role === "ADMIN",
    canManageUsers: role === "ADMIN",
    canExport: role === "ADMIN" || role === "ANALYST",
  };
}
