import { useState, useEffect } from 'react';
import { useLocation, useNavigate, Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar/Sidebar';
import { authService } from '../services/auth';
import './BackofficeLayout.css';

function BackofficeLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const location = useLocation();
  const navigate = useNavigate();

  const sidebarItems = [
    {
      title: 'Navigation',
      links: [
        { label: 'Tableau de bord', href: '/backoffice/dashboard', active: location.pathname === '/backoffice/dashboard' },
        { label: 'Signalements', href: '/backoffice/reports', active: location.pathname.startsWith('/backoffice/reports') },
        { label: 'Entreprises', href: '/backoffice/companies' },
        { label: 'Utilisateurs', href: '/backoffice/users' },
        { label: 'Récapitulatif', href: '/backoffice/summary', active: location.pathname === '/backoffice/summary' },
      ],
    },
    {
      title: 'Administration',
      links: [
        { label: 'Configurations', href: '/backoffice/configurations' },
        {
          label: 'Déconnexion',
          onClick: async () => {
            try {
              await authService.logout();
            } finally {
              navigate('/backoffice');
            }
          }
        },
      ],
    },
  ];

  return (
    <div className="backoffice-layout">
      <Sidebar isOpen={sidebarOpen} items={sidebarItems} onToggle={setSidebarOpen} />
      <main className={`backoffice-main ${sidebarOpen ? 'sidebar-open' : 'sidebar-closed'}`}>
        <Outlet />
      </main>
    </div>
  );
}

export default BackofficeLayout;