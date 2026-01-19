import './Sidebar.css';

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

  return (
    <>
      <aside
        className={`sidebar ${isOpen ? 'sidebar-open' : 'sidebar-closed'} ${className}`}
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
                {item.links && item.links.map((link, linkIndex) => (
                  <a
                    key={linkIndex}
                    href={link.href}
                    className={`sidebar-link ${link.active ? 'sidebar-link-active' : ''}`}
                  >
                    <span className="sidebar-link-text">{link.label}</span>
                  </a>
                ))}
              </nav>
            </div>
          ))}
        </div>

        <button
          className="sidebar-toggle"
          onClick={handleToggle}
          aria-label="Toggle sidebar"
        >
          {'<'}
        </button>
      </aside>

      <div
        className={`sidebar-overlay ${isOpen ? 'sidebar-overlay-visible' : ''}`}
        onClick={handleToggle}
      />
    </>
  );
};

export default Sidebar;
