import { Navigate } from 'react-router-dom';
import { authService } from '../services/auth';

function ProtectedRoute({ children }) {
  const isAuthenticated = authService.isAuthenticated();

  if (!isAuthenticated) {
    // Rediriger vers la page de login si non authentifié
    return <Navigate to="/backoffice" replace />;
  }

  return children;
}

export default ProtectedRoute;
