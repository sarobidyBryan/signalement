import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authService } from '../services/auth';
import { ApiError } from '../services/api';
import { useTheme, THEMES } from '../contexts/ThemeContext';
import PublicNavbar from '../components/PublicNavbar/PublicNavbar';
import Form from '../components/Form/Form';
import FormField from '../components/Form/FormField';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import './css/BackofficeLogin.css';

function BackofficeLogin() {
  const [formData, setFormData] = useState({ email: 'manager@example.com', password: 'managerpass' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const { theme } = useTheme();
  const location = useLocation();

  useEffect(() => {
    const explicitShow = location.state && location.state.showLogin;

    let mounted = true;
    const validateStoredUser = async () => {
      if (explicitShow) return; // user explicitly requested the login form
      const stored = authService.getStoredUser();
      if (!stored) return;
      try {
        const current = await authService.getCurrentUser();
        if (!mounted) return;
        if (current) {
          navigate('/backoffice/summary');
        }
      } catch (e) {
        
      }
    };

    validateStoredUser();
    return () => { mounted = false; };
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    console.debug('Attempting login with email:', formData.email);

    try {
      await authService.login(formData.email, formData.password);
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
    <div className={`backoffice-login ${theme === THEMES.DARK ? 'dark' : 'light'}`}>
      <PublicNavbar />
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
            <div className="password-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Entrez votre mot de passe"
                required
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword((s) => !s)}
                aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
              >
                {showPassword ? (
                  <svg aria-hidden="true" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M3 3L21 21" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M10.94 10.94A3 3 0 0 0 13.06 13.06" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M2.47 12.2C3.9 8.8 7.33 6 12 6c2.03 0 3.9.5 5.33 1.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                    <path d="M21.53 11.8C20.1 15.2 16.67 18 12 18c-2.03 0-3.9-.5-5.33-1.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                ) : (
                  <svg aria-hidden="true" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                    <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                )}
              </button>
            </div>
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