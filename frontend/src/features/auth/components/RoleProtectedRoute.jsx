import { Navigate } from "react-router-dom";
import { useRole } from "../hooks/useRole";

export default function RoleProtectedRoute({ allow, children }) {
  const { role } = useRole();
  if (!role) return <Navigate to="/login" replace />;
  if (!allow.includes(role)) return <Navigate to="/dashboard" replace />;
  return children;
}