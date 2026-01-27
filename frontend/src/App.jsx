import { Routes, Route, Navigate } from 'react-router-dom';
import BackofficeLogin from './pages/BackofficeLogin';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Reports from './pages/Reports';
import ReportEdit from './pages/ReportEdit';
import Summary from './pages/Summary';
import Synchronization from './pages/Synchronization';
import BackofficeLayout from './components/BackofficeLayout';
import ProtectedRoute from './components/ProtectedRoute';
import PublicLayout from './components/PublicLayout';
import Landing from './pages/Landing';
import PublicReports from './pages/PublicReports';
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/backoffice/users" element={<Users />} />
      <Route element={<PublicLayout />}>
        <Route path="/" element={<Landing />} />
        <Route path="/reports" element={<PublicReports />} />
      </Route>
      {/* backoffice */}
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
        <Route path="synchronization" element={<Synchronization />} />
      </Route>
    </Routes>
  );
}

export default App;