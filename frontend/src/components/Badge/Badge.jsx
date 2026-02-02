import '../css/Badge.css';

const Badge = ({
  children,
  variant = 'default',
  size = 'md',
  className = '',
  ...props
}) => {
  const badgeClassName = `badge badge-${variant} badge-${size} ${className}`;

  return (
    <span className={badgeClassName} {...props}>
      {children}
    </span>
  );
};

export default Badge;
