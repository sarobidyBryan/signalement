
import { Outlet, useLocation } from 'react-router-dom';
import { useTheme, THEMES } from '../contexts/ThemeContext';
import PublicNavbar from './PublicNavbar/PublicNavbar';
import './css/PublicLayout.css';

function PublicLayout() {
    const location = useLocation();
    const { theme } = useTheme();
    const isLanding = location.pathname === '/';

    // Landing page handles its own navbar
    if (isLanding) {
        return <Outlet />;
    }

    return (
        <div className={`public-layout ${theme === THEMES.DARK ? 'dark' : 'light'}`}>
            <PublicNavbar />
            <main className="public-main">
                <Outlet />
            </main>
        </div>
    );
}

export default PublicLayout;
