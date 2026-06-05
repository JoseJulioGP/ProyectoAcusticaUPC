import { AuthProvider } from '../features/auth/context/AuthContext';
import { ToastProvider } from '../shared/ui/ToastProvider';
import AppRouter from './router';

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </ToastProvider>
  );
}