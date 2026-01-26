import './Summary.css';
import Map from '../components/Map';

function Summary() {
  return (
    <div className="summary">
      <div className="summary-header">
        <h1>Récapitulatif</h1>
        <p>Vue d'ensemble des signalements sur la carte d'Antananarivo</p>
      </div>

      <div className="summary-content">
        <div className="map-section">
          <Map />
        </div>
        <div className="info-panel">
          <div className="info-panel-header">
            <h2>Informations</h2>
          </div>
          <div className="info-panel-content">
            <div className="info-placeholder">
              <p>Sélectionnez un élément sur la carte pour afficher les informations détaillées.</p>
              <p>Cette zone sera utilisée pour afficher les détails des signalements, statistiques locales, etc.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Summary;