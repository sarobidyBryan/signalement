import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { userService } from '../services';
import Button from '../components/Button/Button';
import './css/UserEdit.css';

const UserEdit = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [roles, setRoles] = useState([]);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    roleCode: '',
    statusCode: 'ACTIVE'
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [user, rolesData] = await Promise.all([
          userService.getById(id),
          userService.getRoles()
        ]);
        
        setFormData({
          name: user.name || '',
          email: user.email || '',
          roleCode: user.role?.roleCode || '',
          statusCode: user.userStatusType?.statusCode || user.status?.code || 'ACTIVE'
        });
        setRoles(rolesData || []);
      } catch (err) {
        console.error('Error fetching user:', err);
        setError('Erreur lors du chargement de l\'utilisateur');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
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
      await userService.update(id, formData);
      navigate('/backoffice/users');
    } catch (err) {
      console.error('Error updating user:', err);
      setError(err.message || 'Erreur lors de la mise à jour de l\'utilisateur');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate('/backoffice/users');
  };

  if (loading) {
    return (
      <div className="user-edit-page">
        <div className="user-edit-loading">Chargement...</div>
      </div>
    );
  }

  return (
    <div className="user-edit-page">
      <div className="user-edit-header">
        <div className="user-edit-header-left">
          <p className="user-edit-overline">Backoffice / Utilisateurs</p>
          <h1>Modifier l'Utilisateur</h1>
        </div>
      </div>

      <div className="user-edit-form-container">
        {error && (
          <div className="user-edit-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="user-edit-form">
          <div className="form-group">
            <label htmlFor="name">Nom *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              placeholder="Entrez le nom"
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
              placeholder="Entrez l'email"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="roleCode">Rôle *</label>
            <select
              id="roleCode"
              name="roleCode"
              value={formData.roleCode}
              onChange={handleChange}
              required
              className="form-select"
            >
              <option value="">Sélectionner un rôle</option>
              {roles.map(role => (
                <option key={role.id || role.roleCode} value={role.roleCode}>
                  {role.label || role.roleCode}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="statusCode">Statut *</label>
            <select
              id="statusCode"
              name="statusCode"
              value={formData.statusCode}
              onChange={handleChange}
              required
              className="form-select"
            >
              <option value="ACTIVE">Actif</option>
              <option value="BLOCKED">Bloqué</option>
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

export default UserEdit;
