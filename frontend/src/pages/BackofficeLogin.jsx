import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Form from '../components/Form/Form';
import FormField from '../components/Form/FormField';
import Button from '../components/Button/Button';
import './BackofficeLogin.css';

function BackofficeLogin() {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    // Simulate login delay
    setTimeout(() => {
      setLoading(false);
      navigate('/dashboard');
    }, 1000);
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
              placeholder="gestionnaire@example.com"
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
          {error && <div className="error-message">{error}</div>}
          <Button type="submit" variant="primary" size="large" disabled={loading}>
            {loading ? 'Connexion en cours...' : 'Se connecter'}
          </Button>
        </Form>
      </div>
    </div>
  );
}

export default BackofficeLogin;