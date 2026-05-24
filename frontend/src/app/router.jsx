import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../features/auth/pages/LoginPage';
import DashboardPage from '../features/dashboard/pages/DashboardPage';
import ProtectedRoute from '../features/auth/components/ProtectedRoute';
import Layout from '../shared/components/Layout';
import IngestionPage from "../features/ingestion/pages/IngestionPage";
import BatchDetailPage from "../features/ingestion/pages/BatchDetailPage";

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

        <Route path="/ingest" element={<IngestionPage />} />
        <Route path="/ingest/:batchId" element={<BatchDetailPage />} />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}