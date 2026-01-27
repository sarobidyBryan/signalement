import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './MapView.css';

export default function MapView({ center = [ -18.8792, 47.5079 ], zoom = 12 }) {
  const mapRef = useRef(null);

  useEffect(() => {
    if (mapRef.current) return; 

    mapRef.current = L.map('map-root', { center, zoom });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(mapRef.current);

    return () => {
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
      }
    };
  }, [center, zoom]);

  return <div id="map-root" className="map-root" />;
}
