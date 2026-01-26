import { Routes, Route, Navigate } from 'react-router-dom';
import BackofficeLogin from './pages/BackofficeLogin';
import Dashboard from './pages/Dashboard';
import Reports from './pages/Reports';
import ReportEdit from './pages/ReportEdit';
import Summary from './pages/Summary';
import BackofficeLayout from './components/BackofficeLayout';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/backoffice" replace />} />
      <Route path="/backoffice" element={<BackofficeLogin />} />
      <Route
        path="/backoffice/*"
        element={
          <ProtectedRoute>
            <BackofficeLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="reports" element={<Reports />} />
        <Route path="reports/:id/edit" element={<ReportEdit />} />
        <Route path="summary" element={<Summary />} />
      </Route>
    </Routes>
  );
}

export default App;