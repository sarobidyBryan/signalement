import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { companyService } from '../services';
import Button from '../components/Button/Button';
import './css/CompanyEdit.css';

const CompanyEdit = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    email: ''
  });

  useEffect(() => {
    const fetchCompany = async () => {
      try {
        const company = await companyService.getById(id);
        setFormData({
          name: company.name || '',
          email: company.email || ''
        });
      } catch (err) {
        console.error('Error fetching company:', err);
        setError('Erreur lors du chargement de l\'entreprise');
      } finally {
        setLoading(false);
      }
    };

    fetchCompany();
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      await companyService.update(id, formData);
      navigate('/backoffice/companies');
    } catch (err) {
      console.error('Error updating company:', err);
      setError(err.message || 'Erreur lors de la mise à jour de l\'entreprise');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate('/backoffice/companies');
  };

  if (loading) {
    return (
      <div className="company-edit-page">
        <div className="company-edit-loading">Chargement...</div>
      </div>
    );
  }

  return (
    <div className="company-edit-page">
      <div className="company-edit-header">
        <div className="company-edit-header-left">
          <p className="company-edit-overline">Backoffice / Entreprises</p>
          <h1>Modifier l'Entreprise</h1>
        </div>
      </div>

      <div className="company-edit-form-container">
        {error && (
          <div className="company-edit-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="company-edit-form">
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

export default CompanyEdit;
