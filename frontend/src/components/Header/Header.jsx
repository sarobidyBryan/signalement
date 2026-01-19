import { useState } from 'react';
import './Header.css';

const Header = ({
  title,
  logo,
  onMenuToggle,
  navItems = [],
  className = '',
  ...props
}) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const handleMenuToggle = () => {
    setIsMenuOpen(!isMenuOpen);
    onMenuToggle && onMenuToggle(!isMenuOpen);
  };

  return (
    <header className={`header ${className}`} {...props}>
      <div className="header-container">
        <div className="header-logo">
          {logo && <img src={logo} alt="Logo" className="logo-image" />}
          {title && <span className="header-title">{title}</span>}
        </div>

        <nav className={`header-nav ${isMenuOpen ? 'header-nav-open' : ''}`}>
          {navItems.map((item, index) => (
            <a key={index} href={item.href} className="nav-item">
              {item.label}
            </a>
          ))}
        </nav>

        <button
          className="header-menu-toggle"
          onClick={handleMenuToggle}
          aria-label="Toggle menu"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </header>
  );
};

export default Header;
