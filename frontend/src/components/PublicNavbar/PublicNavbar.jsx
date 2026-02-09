import { useNavigate, useLocation } from 'react-router-dom';
import { useTheme, THEMES } from '../../contexts/ThemeContext';
import ThemeToggle from '../ThemeToggle';
import '../css/PublicNavbar.css';

function PublicNavbar({ variant = 'default' }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { theme } = useTheme();
  
  const isLanding = location.pathname === '/';
  const isDark = theme === THEMES.DARK;

  return (
    <nav className={`public-navbar ${isDark ? 'dark' : 'light'} ${variant}`}>
      <div className="public-navbar-logo" onClick={() => navigate('/')}>
        Signaleo<span className="logo-exclamation">!</span>
      </div>
      <div className="public-navbar-buttons">
        <ThemeToggle className="navbar-variant" />
        {!isLanding && (
          <button 
            className="btn-nav btn-nav-ghost" 
            onClick={() => navigate('/')}
          >
            Accueil
          </button>
        )}
        <button 
          className="btn-nav btn-nav-ghost" 
          onClick={() => navigate('/reports')}
        >
          Explorer
        </button>
        <button 
          className="btn-nav btn-nav-primary" 
          onClick={() => navigate('/backoffice', { state: { showLogin: true } })}
        >
          Se connecter
        </button>
      </div>
    </nav>
  );
}

export default PublicNavbar;
