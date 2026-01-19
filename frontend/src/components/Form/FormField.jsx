import './FormField.css';

const FormField = ({
  label,
  error,
  required,
  children,
  className = '',
  ...props
}) => {
  return (
    <div className={`form-field ${error ? 'form-field-error' : ''} ${className}`} {...props}>
      {label && (
        <label className="form-label">
          {label}
          {required && <span className="form-label-required">*</span>}
        </label>
      )}
      {children}
      {error && <span className="form-field-error-message">{error}</span>}
    </div>
  );
};

export default FormField;
