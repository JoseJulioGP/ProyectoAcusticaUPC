import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useRole } from "../hooks/useRole";

// Landing por rol para denegaciones, evita bucles de redirección
// (p.ej. ANALYST no puede ver /dashboard → no debe re-redirigir a /dashboard).
const landingFor = (role) => (role === "ANALYST" ? "/admin/zones" : "/dashboard");

export default function RoleProtectedRoute({ allow, children }) {
  const { loading, isAuthenticated } = useAuth();
  const { role } = useRole();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="text-slate-500">Cargando sesión…</div>
      </div>
    );
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!allow.includes(role)) return <Navigate to={landingFor(role)} replace />;
  return children;
}
