import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { companyService } from '../services';
import Button from '../components/Button/Button';
import './css/CompanyCreate.css';

const CompanyCreate = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    email: ''
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
      await companyService.create(formData);
      navigate('/backoffice/companies');
    } catch (err) {
      console.error('Error creating company:', err);
      setError(err.message || 'Erreur lors de la création de l\'entreprise');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/backoffice/companies');
  };

  return (
    <div className="company-create-page">
      <div className="company-create-header">
        <div className="company-create-header-left">
          <p className="company-create-overline">Backoffice / Entreprises</p>
          <h1>Nouvelle Entreprise</h1>
        </div>
      </div>

      <div className="company-create-form-container">
        {error && (
          <div className="company-create-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="company-create-form">
          <div className="form-group">
            <label htmlFor="name">Nom *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              placeholder="Entrez le nom de l'entreprise"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email *</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              placeholder="Entrez l'email de l'entreprise"
              className="form-input"
            />
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
              {loading ? 'Création...' : 'Créer'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CompanyCreate;
