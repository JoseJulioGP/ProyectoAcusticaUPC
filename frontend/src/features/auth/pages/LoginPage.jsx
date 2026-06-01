import { Navigate } from 'react-router-dom';
import LoginForm from '../components/LoginForm';
import { useAuth } from '../hooks/useAuth';

export default function LoginPage() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null;
  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  return <LoginForm />;
}