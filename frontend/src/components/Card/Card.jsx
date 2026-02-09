import '../css/Card.css';

const Card = ({
  children,
  title,
  subtitle,
  footer,
  className = '',
  onClick,
  hoverable = false,
  ...props
}) => {
  const cardClassName = `card ${hoverable ? 'card-hoverable' : ''} ${className}`;

  return (
    <div className={cardClassName} onClick={onClick} {...props}>
      {(title || subtitle) && (
        <div className="card-header">
          {title && <h3 className="card-title">{title}</h3>}
          {subtitle && <p className="card-subtitle">{subtitle}</p>}
        </div>
      )}
      <div className="card-content">{children}</div>
      {footer && <div className="card-footer">{footer}</div>}
    </div>
  );
};

export default Card;
