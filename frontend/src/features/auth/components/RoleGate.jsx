import { useRole } from "../hooks/useRole";

/**
 * Renderiza children solo si el rol del usuario está en `allow`.
 */

export default function RoleGate({ allow, children, fallback = null }) {
  const { role } = useRole();
  if (!role || !allow.includes(role)) return fallback;
  return children;
}