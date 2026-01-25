import { Routes, Route, Navigate } from 'react-router-dom';
import BackofficeLogin from './pages/BackofficeLogin';
import Dashboard from './pages/Dashboard';
import Reports from './pages/Reports';
import ReportEdit from './pages/ReportEdit';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/backoffice" replace />} />
      <Route path="/backoffice" element={<BackofficeLogin />} />
      <Route
        path="/backoffice/dashboard"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/backoffice/reports"
        element={
          <ProtectedRoute>
            <Reports />
          </ProtectedRoute>
        }
      />
      <Route
        path="/backoffice/reports/:id/edit"
        element={
          <ProtectedRoute>
            <ReportEdit />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;