import { useState, useEffect } from 'react';
import { useLocation, useNavigate, Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar/Sidebar';
import { authService } from '../services/auth';
import './css/BackofficeLayout.css';

function BackofficeLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const location = useLocation();
  const navigate = useNavigate();

  const sidebarItems = [
    {
      title: 'Navigation',
      links: [
        { label: 'Récapitulatif', href: '/backoffice/summary', active: location.pathname === '/backoffice/summary' },
        { label: 'Signalements', href: '/backoffice/reports', active: location.pathname.startsWith('/backoffice/reports') },
        { label: 'Délais', href: '/backoffice/delais', active: location.pathname === '/backoffice/delais' },
        { label: 'Entreprises', href: '/backoffice/companies', active: location.pathname.startsWith('/backoffice/companies') },
        { label: 'Utilisateurs', href: '/backoffice/users', active: location.pathname.startsWith('/backoffice/users') },
        { label: 'Synchronisation', href: '/backoffice/synchronization', active: location.pathname === '/backoffice/synchronization' },
      ],
    },
    {
      title: 'Administration',
      links: [
        { label: 'Configurations', href: '/backoffice/configurations', active: location.pathname.startsWith('/backoffice/configurations') },
        {
          label: 'Déconnexion',
          onClick: async () => {
            const confirmed = window.confirm('Êtes-vous sûr de vouloir vous déconnecter ?');
            if (!confirmed) return;
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