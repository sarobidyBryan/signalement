import { Routes, Route, Navigate } from 'react-router-dom';
import BackofficeLogin from './pages/BackofficeLogin';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import UserCreate from './pages/UserCreate';
import UserEdit from './pages/UserEdit';
import Reports from './pages/Reports';
import ReportEdit from './pages/ReportEdit';
import Summary from './pages/Summary';
import Synchronization from './pages/Synchronization';
import Configurations from './pages/Configurations';
import ConfigurationCreate from './pages/ConfigurationCreate';
import ConfigurationEdit from './pages/ConfigurationEdit';
import Companies from './pages/Companies';
import CompanyCreate from './pages/CompanyCreate';
import CompanyEdit from './pages/CompanyEdit';
import StatReports from './pages/StatReports';
import BackofficeLayout from './components/BackofficeLayout';
import ProtectedRoute from './components/ProtectedRoute';
import PublicLayout from './components/PublicLayout';
import Landing from './pages/Landing';
import PublicReports from './pages/PublicReports';
import './css/App.css'

function App() {
  return (
    <Routes>
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
        <Route path="delais" element={<StatReports />} />
        <Route path="summary" element={<Summary />} />
        <Route path="synchronization" element={<Synchronization />} />
        <Route path="users" element={<Users />} />
        <Route path="users/new" element={<UserCreate />} />
        <Route path="users/:id/edit" element={<UserEdit />} />
        <Route path="configurations" element={<Configurations />} />
        <Route path="configurations/new" element={<ConfigurationCreate />} />
        <Route path="configurations/:key/edit" element={<ConfigurationEdit />} />
        <Route path="companies" element={<Companies />} />
        <Route path="companies/new" element={<CompanyCreate />} />
        <Route path="companies/:id/edit" element={<CompanyEdit />} />
      </Route>
    </Routes>
  );
}

export default App;