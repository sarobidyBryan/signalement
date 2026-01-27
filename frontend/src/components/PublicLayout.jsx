
import { Outlet, useLocation } from 'react-router-dom';
import Header from './Header/Header';
import './PublicLayout.css';

function PublicLayout() {
    const location = useLocation();

    const navItems = [
        { href: '/', label: 'Accueil' },
        { href: '/reports', label: 'Signalements' }
    ];

    return (
        <div className="public-layout">
            <Header
                title="Signaleo"
                navItems={navItems}
                className="public-header"
            />
            <main className="public-main">
                <Outlet />
            </main>
        </div>
    );
}

export default PublicLayout;
