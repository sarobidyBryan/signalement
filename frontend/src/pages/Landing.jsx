
import { useNavigate } from 'react-router-dom';
import './Landing.css'; // Uses cloned Summary styles
import Button from '../components/Button/Button';

function Landing() {
  const navigate = useNavigate();

  return (
    <div className="summary">
      <div className="summary-header">
        <h1>Bienvenue sur Signaleo</h1>
        <p>La plateforme collaborative pour l'amélioration des infrastructures routières</p>
      </div>

      <div className="summary-content">
        <div className="map-section fullwidth" style={{ height: 'auto', minHeight: '400px', alignItems: 'center', justifyContent: 'center' }}>
          <div className="landing-cta">
            <h2>Participez à l'amélioration de votre ville</h2>
            <p>
              Signalez les problèmes de voirie, suivez l'avancement des réparations
              et consultez les statistiques en temps réel sur notre carte interactive.
            </p>
            <div style={{ display: 'flex', gap: '16px', marginTop: '20px' }}>
              <Button variant="primary" size="lg" onClick={() => navigate('/reports')}>
                Consulter la carte
              </Button>
              {/* Optional secondary CTA */}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Landing;
