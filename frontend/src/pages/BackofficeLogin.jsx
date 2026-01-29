import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { ApiError } from '../services/api';
import Form from '../components/Form/Form';
import FormField from '../components/Form/FormField';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import './BackofficeLogin.css';

function BackofficeLogin() {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Si déjà connecté, rediriger vers le récapitulatif
    if (authService.isAuthenticated()) {
      navigate('/backoffice/summary');
    }
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    // Effacer l'erreur lors de la modification
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    // Debug: log login attempt
    console.debug('Attempting login with email:', formData.email);

    try {
      await authService.login(formData.email, formData.password);
      // Rediriger vers le récapitulatif après connexion réussie
      navigate('/backoffice/summary');
    } catch (err) {
      if (err instanceof ApiError) {
        setError({ message: err.message, errorCode: err.errorCode, status: err.status });
      } else {
        setError({ message: err.message || 'Email ou mot de passe incorrect' });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="backoffice-login">
      <div className="login-container">
        <div className="login-header">
          <h1>Signaleo</h1>
          <p>Accès Backoffice</p>
        </div>
        <Form onSubmit={handleSubmit} className="login-form">
          <FormField label="Email" required>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="manager@example.com"
              required
            />
          </FormField>
          <FormField label="Mot de passe" required>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Entrez votre mot de passe"
              required
            />
          </FormField>
          {error && <ErrorBanner error={error} />}
          <Button type="submit" variant="primary" size="large" disabled={loading}>
            {loading ? 'Connexion en cours...' : 'Se connecter'}
          </Button>
        </Form>
      </div>
    </div>
  );
}

export default BackofficeLogin;