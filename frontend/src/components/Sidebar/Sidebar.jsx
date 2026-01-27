import './Sidebar.css';
import { NavLink } from 'react-router-dom';

const Sidebar = ({
  items = [],
  isOpen = true,
  onToggle,
  className = '',
  ...props
}) => {
  const handleToggle = () => {
    onToggle && onToggle(!isOpen);
  };

  const isDesktop = window.innerWidth > 768;
  const effectiveIsOpen = isDesktop ? true : isOpen;

  return (
    <>
      <aside
        className={`sidebar ${effectiveIsOpen ? 'sidebar-open' : 'sidebar-closed'} ${className}`}
        {...props}
      >
        <div className="sidebar-content">
          <div className="sidebar-header">
            <h2>Signaleo</h2>
          </div>
          {items.map((item, index) => (
            <div key={index} className="sidebar-section">
              {item.title && (
                <h4 className="sidebar-section-title">{item.title}</h4>
              )}
              <nav className="sidebar-nav">
                {item.links && item.links.map((link, linkIndex) => {
                  const key = link.key || (link.label || '').toLowerCase();
                  const href = link.href || '';
                  const isActive = props.selected === key;
                  const hasOnSelect = typeof props.onSelect === 'function';

                  if (hasOnSelect) {
                    return (
                      <a
                        key={linkIndex}
                        href={href}
                        className={`sidebar-link ${isActive ? 'sidebar-link-active' : ''}`}
                        onClick={(e) => { e.preventDefault(); props.onSelect({...link, key}); }}
                      >
                        <span className="sidebar-link-text">{link.label}</span>
                      </a>
                    );
                  }

                  // Otherwise, if it's an internal route, use NavLink for SPA routing
                  if (href.startsWith('/')) {
                    return (
                      <NavLink
                        key={linkIndex}
                        to={href}
                        className={({ isActive: navIsActive }) => `sidebar-link ${navIsActive || isActive ? 'sidebar-link-active' : ''}`}
                      >
                        <span className="sidebar-link-text">{link.label}</span>
                      </NavLink>
                    );
                  }

                  // Fallback: external or hash links
                  return (
                    <a key={linkIndex} href={href} className={`sidebar-link ${isActive ? 'sidebar-link-active' : ''}`}>
                      <span className="sidebar-link-text">{link.label}</span>
                    </a>
                  );
                })}
                {item.links && item.links.map((link, linkIndex) => (
                  link.onClick ? (
                    <button
                      key={linkIndex}
                      onClick={(e) => {
                        e.preventDefault();
                        link.onClick();
                      }}
                      className={`sidebar-link ${link.active ? 'sidebar-link-active' : ''}`}
                    >
                      <span className="sidebar-link-text">{link.label}</span>
                    </button>
                  ) : (
                    <a
                      key={linkIndex}
                      href={link.href}
                      className={`sidebar-link ${link.active ? 'sidebar-link-active' : ''}`}
                    >
                      <span className="sidebar-link-text">{link.label}</span>
                    </a>
                  )
                ))}
              </nav>
            </div>
          ))}
        </div>
      </aside>

      <div
        className={`sidebar-overlay ${effectiveIsOpen ? 'sidebar-overlay-visible' : ''}`}
        onClick={isDesktop ? undefined : handleToggle}
      />
    </>
  );
};

export default Sidebar;
