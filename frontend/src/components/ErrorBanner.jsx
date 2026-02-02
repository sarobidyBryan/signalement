import './css/ErrorBanner.css';

export default function ErrorBanner({ error }) {
  if (!error) return null;

  const message = typeof error === 'string' ? error : error.message || 'Une erreur est survenue';
  const code = error.code || error.errorCode || error.status || null;

  return (
    <div className="error-banner" role="alert">
      <div className="error-banner-icon">⚠️</div>
      <div className="error-banner-content">
        <div className="error-banner-message">{message}</div>
        {code && <div className="error-banner-code">Code: {code}</div>}
      </div>
    </div>
  );
}
