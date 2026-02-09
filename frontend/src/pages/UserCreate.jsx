import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import { userService } from '../services';
import { ApiError } from '../services/api';
import './css/UserCreate.css';

export default function UserCreate() {
  const navigate = useNavigate();
  const [roles, setRoles] = useState([]);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    roleCode: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRoles = async () => {
      try {
        const rolesData = await userService.getRoles();
        setRoles(rolesData || []);
        if (rolesData && rolesData.length > 0) {
          setFormData(prev => ({ ...prev, roleCode: rolesData[0].roleCode }));
        }
      } catch (err) {
        console.error('Error fetching roles:', err);
        // Fallback to default roles
        setRoles([
          { roleCode: 'USER', label: 'Utilisateur' },
          { roleCode: 'MANAGER', label: 'Manager' },
        ]);
        setFormData(prev => ({ ...prev, roleCode: 'USER' }));
      }
    };
    fetchRoles();
  }, []);

  const handleError = (err) => {
    if (err instanceof ApiError) {
      setError({ message: err.message, errorCode: err.errorCode, status: err.status });
    } else {
      setError({ message: err?.message || 'Erreur de connexion au serveur' });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await userService.create({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        roleCode: formData.roleCode,
        statusCode: 'ACTIVE', // Default status
      });
      navigate('/backoffice/users');
    } catch (err) {
      handleError(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field) => (e) => {
    setFormData((prev) => ({ ...prev, [field]: e.target.value }));
  };

  return (
    <div className="user-create-page">
      <div className="user-create-header">
        <div className="user-create-header-left">
          <p className="user-create-overline">Gestion des utilisateurs</p>
          <h1>Nouvel utilisateur</h1>
        </div>
        <Button variant="secondary" onClick={() => navigate('/backoffice/users')}>
          ← Retour
        </Button>
      </div>

      {error && <ErrorBanner error={error} onDismiss={() => setError(null)} />}

      <div className="user-create-form-container">
        <form onSubmit={handleSubmit} className="user-create-form">
          <div className="form-group">
            <label htmlFor="name">Nom</label>
            <input
              id="name"
              type="text"
              value={formData.name}
              onChange={handleChange('name')}
              placeholder="Entrez le nom"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={formData.email}
              onChange={handleChange('email')}
              placeholder="Entrez l'email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mot de passe</label>
            <input
              id="password"
              type="password"
              value={formData.password}
              onChange={handleChange('password')}
              placeholder="Entrez le mot de passe"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="role">Rôle</label>
            <select
              id="role"
              value={formData.roleCode}
              onChange={handleChange('roleCode')}
              required
            >
              {roles.map((role) => (
                <option key={role.roleCode || role.code} value={role.roleCode || role.code}>
                  {role.label || role.roleCode}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Statut</label>
            <div className="status-info">
              <span className="status-badge active">Actif</span>
              <span className="status-note">Le statut par défaut est "Actif"</span>
            </div>
          </div>

          <div className="form-actions">
            <Button type="button" variant="secondary" onClick={() => navigate('/backoffice/users')}>
              Annuler
            </Button>
            <Button type="submit" variant="primary" disabled={loading}>
              {loading ? 'Création...' : 'Créer l\'utilisateur'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
