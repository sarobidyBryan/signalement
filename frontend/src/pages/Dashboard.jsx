import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { ApiError } from '../services/api';
import { dashboardService } from '../services';
import Card from '../components/Card/Card';
import ErrorBanner from '../components/ErrorBanner';
import './Dashboard.css';

function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null); // error can be ApiError or object with message
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      // Récupérer l'utilisateur connecté
      const currentUser = authService.getStoredUser();
      setUser(currentUser);

      // Récupérer les statistiques
      const statsData = await dashboardService.getStats();
      setStats(statsData);
    } catch (err) {
      if (err instanceof ApiError) {
        setError({ message: err.message, errorCode: err.errorCode, status: err.status });
      } else {
        setError({ message: err.message || 'Erreur lors du chargement des données' });
      }
      console.error('Dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Fonction pour formater les nombres avec des séparateurs
  const formatNumber = (num) => {
    return num?.toLocaleString('fr-FR') || '0';
  };

  const statsConfig = stats ? [
    { 
      title: 'Total Signalements', 
      value: formatNumber(stats.totalReports),
      icon: '📊'
    },
    { 
      title: 'Signalements Assignés', 
      value: formatNumber(stats.assignedReports),
      icon: '📋'
    },
    { 
      title: 'En Cours', 
      value: formatNumber(stats.inProgressReports),
      icon: '⏳'
    },
    { 
      title: 'Terminés', 
      value: formatNumber(stats.completedReports),
      icon: '✅'
    },
    { 
      title: 'En Attente', 
      value: formatNumber(stats.pendingReports),
      icon: '⏰'
    },
    { 
      title: 'Entreprises', 
      value: formatNumber(stats.totalCompanies),
      icon: '🏢'
    },
    { 
      title: 'Utilisateurs Actifs', 
      value: formatNumber(stats.activeUsers),
      icon: '👥'
    },
  ] : [];

  return (
    <div className="dashboard">
      <div className="main-header">
        <div className="header-title">
          <h1>Tableau de bord</h1>
          {user && <p className="welcome-text">Bienvenue, {user.name}</p>}
        </div>
      </div>

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Chargement des données...</p>
        </div>
      ) : error ? (
        <div className="error-container">
          <ErrorBanner error={error} />
          <button onClick={loadDashboardData} className="retry-btn">Réessayer</button>
        </div>
      ) : (
        <>
          <div className="stats-grid">
            {statsConfig.map((stat, index) => (
              <Card key={index} className="stat-card">
                <div className="stat-content">
                  <div className="stat-icon">{stat.icon}</div>
                  <div className="stat-info">
                    <div className="stat-value">{stat.value}</div>
                    <div className="stat-title">{stat.title}</div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
          <div className="global-view-container">
            <h2>Vue globale</h2>
            <p className="info-text">Système de gestion des signalements - Tableau de bord administrateur</p>
          </div>
        </>
      )}
    </div>
  );
}

export default Dashboard;