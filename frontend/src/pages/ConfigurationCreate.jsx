import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { configurationService } from '../services';
import Button from '../components/Button/Button';
import './ConfigurationCreate.css';

const ConfigurationCreate = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    key: '',
    value: '',
    type: 'STRING'
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await configurationService.create(formData);
      navigate('/backoffice/configurations');
    } catch (err) {
      console.error('Error creating configuration:', err);
      setError(err.message || 'Erreur lors de la création de la configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/backoffice/configurations');
  };

  return (
    <div className="configuration-create-page">
      <div className="configuration-create-header">
        <div className="configuration-create-header-left">
          <p className="configuration-create-overline">Backoffice / Configurations</p>
          <h1>Nouvelle Configuration</h1>
        </div>
      </div>

      <div className="configuration-create-form-container">
        {error && (
          <div className="configuration-create-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="configuration-create-form">
          <div className="form-group">
            <label htmlFor="key">Clé *</label>
            <input
              type="text"
              id="key"
              name="key"
              value={formData.key}
              onChange={handleChange}
              required
              placeholder="Entrez la clé de configuration"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="value">Valeur *</label>
            <textarea
              id="value"
              name="value"
              value={formData.value}
              onChange={handleChange}
              required
              placeholder="Entrez la valeur"
              className="form-input form-textarea"
              rows={4}
            />
          </div>

          <div className="form-group">
            <label htmlFor="type">Type *</label>
            <select
              id="type"
              name="type"
              value={formData.type}
              onChange={handleChange}
              required
              className="form-select"
            >
              <option value="STRING">String</option>
              <option value="NUMBER">Number</option>
              <option value="BOOLEAN">Boolean</option>
              <option value="JSON">JSON</option>
            </select>
          </div>

          <div className="form-actions">
            <Button
              type="button"
              variant="secondary"
              onClick={handleCancel}
              disabled={loading}
            >
              Annuler
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={loading}
            >
              {loading ? 'Création...' : 'Créer la configuration'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ConfigurationCreate;
