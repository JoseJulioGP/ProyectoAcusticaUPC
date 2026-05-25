import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../features/auth/pages/LoginPage';
import DashboardPage from '../features/dashboard/pages/DashboardPage';
import IngestionPage from '../features/ingestion/pages/IngestionPage';
import BatchDetailPage from '../features/ingestion/pages/BatchDetailPage';
import ProtectedRoute from '../features/auth/components/ProtectedRoute';
import Layout from '../shared/components/Layout';

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Layout>
                <DashboardPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/ingest"
          element={
            <ProtectedRoute>
              <Layout>
                <IngestionPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/ingest/:batchId"
          element={
            <ProtectedRoute>
              <Layout>
                <BatchDetailPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}