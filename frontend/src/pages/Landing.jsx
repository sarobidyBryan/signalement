import { useNavigate } from 'react-router-dom';
import './Landing.css';
import Button from '../components/Button/Button';

export default function Landing() {
  const navigate = useNavigate();

  const handleSeeReports = () => navigate('/reports');

  return (
    <main className="landing">
      <section className="landing-hero">
        <div className="landing-content">
          <h1>Explorez l'évolution des travaux et problèmes routiers à Antananarivo</h1>
          <p>
            Suivez les chantiers, signalez et consultez l'avancement des interventions sur
            une carte interactive — toutes les données sont accessibles hors-ligne.
          </p>
          <Button variant="primary" size="lg" onClick={handleSeeReports}>
            Voir <span className="cta-arrow">→</span>
          </Button>
        </div>
      </section>
    </main>
  );
}
