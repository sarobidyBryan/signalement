import { Routes, Route } from 'react-router-dom';
import BackofficeLogin from './pages/BackofficeLogin';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/backoffice/users" element={<Users/>} />
      <Route path="/backoffice" element={<BackofficeLogin />} />
      <Route path="/backoffice/dashboard" element={<Dashboard />} />
    </Routes>
  );
}

export default App;