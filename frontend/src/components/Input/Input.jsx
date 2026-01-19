import './Input.css';

const Input = ({
  type = 'text',
  placeholder = '',
  value,
  onChange,
  disabled = false,
  error = false,
  helperText = '',
  size = 'md',
  className = '',
  ...props
}) => {
  const inputClassName = `input input-${size} ${error ? 'input-error' : ''} ${className}`;

  return (
    <div className="input-wrapper">
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        disabled={disabled}
        className={inputClassName}
        {...props}
      />
      {helperText && (
        <span className={`helper-text ${error ? 'helper-text-error' : ''}`}>
          {helperText}
        </span>
      )}
    </div>
  );
};

export default Input;
