import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { configurationService } from '../services';
import Button from '../components/Button/Button';
import './css/ConfigurationEdit.css';

const ConfigurationEdit = () => {
  const navigate = useNavigate();
  const { key } = useParams();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    key: '',
    value: '',
    type: 'STRING'
  });

  useEffect(() => {
    const fetchConfiguration = async () => {
      try {
        const config = await configurationService.getByKey(key);
        setFormData({
          key: config.key || '',
          value: config.value || '',
          type: config.type || 'STRING'
        });
      } catch (err) {
        console.error('Error fetching configuration:', err);
        setError('Erreur lors du chargement de la configuration');
      } finally {
        setLoading(false);
      }
    };

    fetchConfiguration();
  }, [key]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      await configurationService.update(key, {
        value: formData.value,
        type: formData.type
      });
      navigate('/backoffice/configurations');
    } catch (err) {
      console.error('Error updating configuration:', err);
      setError(err.message || 'Erreur lors de la mise à jour de la configuration');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate('/backoffice/configurations');
  };

  if (loading) {
    return (
      <div className="configuration-edit-page">
        <div className="configuration-edit-loading">Chargement...</div>
      </div>
    );
  }

  return (
    <div className="configuration-edit-page">
      <div className="configuration-edit-header">
        <div className="configuration-edit-header-left">
          <p className="configuration-edit-overline">Backoffice / Configurations</p>
          <h1>Modifier la Configuration</h1>
        </div>
      </div>

      <div className="configuration-edit-form-container">
        {error && (
          <div className="configuration-edit-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="configuration-edit-form">
          <div className="form-group">
            <label htmlFor="key">Clé</label>
            <input
              type="text"
              id="key"
              name="key"
              value={formData.key}
              disabled
              className="form-input form-input-disabled"
            />
            <small className="form-hint">La clé ne peut pas être modifiée</small>
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
              disabled={saving}
            >
              Annuler
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={saving}
            >
              {saving ? 'Enregistrement...' : 'Enregistrer'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ConfigurationEdit;
