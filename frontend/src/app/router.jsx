import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../features/auth/pages/LoginPage';
import RegisterPage from '../features/auth/pages/RegisterPage';
import DashboardPage from '../features/analytics/pages/DashboardPage';
import ZoneDetailPage from '../features/analytics/pages/ZoneDetailPage';
import NoiseMapPage from '../features/analytics/pages/NoiseMapPage';
import IngestionPage from '../features/ingestion/pages/IngestionPage';
import BatchDetailPage from '../features/ingestion/pages/BatchDetailPage';
import ProtectedRoute from '../features/auth/components/ProtectedRoute';
import Layout from '../shared/components/Layout';
import AlertsPage from "../features/compliance/pages/AlertsPage";
import { CompliancePage } from "../features/compliance/pages/CompliancePage";
import RoleProtectedRoute from '../features/auth/components/RoleProtectedRoute';
import UserManagementPage from '../features/admin/pages/UserManagementPage';
import ZonesAdminPage from '../features/zones/pages/ZonesAdminPage';

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/dashboard"
          element={
            <RoleProtectedRoute allow={["ADMIN", "ANALYST", "VIEWER"]}>
              <Layout>
                <DashboardPage />
              </Layout>
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/dashboard/zones/:zoneId"
          element={
            <RoleProtectedRoute allow={["ADMIN", "ANALYST", "VIEWER"]}>
              <Layout>
                <ZoneDetailPage />
              </Layout>
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/noise-map"
          element={
            <RoleProtectedRoute allow={["ADMIN", "ANALYST", "VIEWER"]}>
              <Layout>
                <NoiseMapPage />
              </Layout>
            </RoleProtectedRoute>
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
        <Route
          path="/alerts"
          element={
            <ProtectedRoute>
              <Layout>
                <AlertsPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/compliance"
          element={
            <RoleProtectedRoute allow={["ADMIN", "ANALYST", "VIEWER"]}>
              <Layout>
                <CompliancePage />
              </Layout>
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/zones"
          element={
            <RoleProtectedRoute allow={["ADMIN", "ANALYST", "VIEWER"]}>
              <Layout>
                <ZonesAdminPage />
              </Layout>
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <RoleProtectedRoute allow={["ADMIN"]}>
              <Layout>
                <UserManagementPage />
              </Layout>
            </RoleProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}