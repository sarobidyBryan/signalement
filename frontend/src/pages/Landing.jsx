import { useNavigate } from 'react-router-dom';
import { useTheme, THEMES } from '../contexts/ThemeContext';
import PublicNavbar from '../components/PublicNavbar/PublicNavbar';
import './css/Landing.css';

function Landing() {
  const navigate = useNavigate();
  const { theme } = useTheme();

  return (
    <div className={`landing-page ${theme === THEMES.DARK ? 'dark' : 'light'}`}>
      {/* Floating orbs for visual effect */}
      <div className="orb orb-1"></div>
      <div className="orb orb-2"></div>
      <div className="orb orb-3"></div>

      {/* Navbar */}
      <PublicNavbar />

      {/* Hero Section */}
      <section className="landing-hero">
        <div className="hero-content">
          
          <h1 className="hero-title">
            Améliorons ensemble<br />
            <span className="highlight">nos infrastructures</span>
          </h1>
          
          <p className="hero-subtitle">
            Signalez les problèmes de voirie, suivez l'avancement des réparations 
            et consultez les statistiques en temps réel sur notre carte interactive.
          </p>

          <div className="hero-cta">
            <button 
              className="btn-hero btn-hero-primary" 
              onClick={() => navigate('/reports')}
            >
              Explorer la carte
              <span className="btn-arrow">→</span>
            </button>
            <button 
              className="btn-hero btn-hero-secondary" 
              onClick={() => navigate('/backoffice')}
            >
              Se connecter
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Landing;
