import { useState } from 'react';
import Sidebar from '../components/Sidebar/Sidebar';
import Card from '../components/Card/Card';
import './Dashboard.css';

function Dashboard() {
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const sidebarItems = [
    {
      title: 'Navigation',
      links: [
        { label: 'Tableau de bord', href: '#', active: true },
        { label: 'Signalements', href: '#' },
        { label: 'Entreprises', href: '#' },
        { label: 'Utilisateurs', href: '#' },
      ],
    },
    {
      title: 'Administration',
      links: [
        { label: 'Configurations', href: '#' },
        { label: 'Déconnexion', href: '/backoffice' },
      ],
    },
  ];

  const stats = [
    { title: 'Total Signalements', value: '1,234' },
    { title: 'Signalements Assignés', value: '856' },
    { title: 'En Cours', value: '234' },
    { title: 'Terminés', value: '622' },
    { title: 'Entreprises', value: '7' },
    { title: 'Utilisateurs Actifs', value: '156' },
  ];

  return (
    <div className="dashboard">
      <div className="dashboard-content">
        <Sidebar isOpen={sidebarOpen} items={sidebarItems} onToggle={setSidebarOpen} />
        <main className={`dashboard-main ${sidebarOpen ? 'sidebar-open' : 'sidebar-closed'}`}>
          <div className="main-header">
            <button className="hamburger-btn" onClick={() => setSidebarOpen(!sidebarOpen)}>
              <span></span>
              <span></span>
              <span></span>
            </button>
            <h1>Tableau de bord</h1>
          </div>
          <div className="stats-grid">
            {stats.map((stat, index) => (
              <Card key={index} className="stat-card">
                <div className="stat-content">
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
          </div>
        </main>
      </div>
    </div>
  );
}

export default Dashboard;