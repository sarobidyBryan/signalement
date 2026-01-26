import { useEffect, useRef } from 'react';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import './Map.css';

const Map = ({ className = '' }) => {
  const mapContainer = useRef(null);
  const map = useRef(null);

  useEffect(() => {
    if (map.current) return; // initialize map only once

    map.current = new maplibregl.Map({
      container: mapContainer.current,
      style: '/styles/osm-bright/style.json', // tileserver style URL
      center: [47.519, -18.879], // center of Antananarivo
      zoom: 12,
      attributionControl: false,
      transformRequest: (url, resourceType) => {
        // Fix for "Failed to parse URL" in MapLibre workers when using relative paths
        if (url.startsWith('/')) {
          return {
            url: `${window.location.origin}${url}`
          };
        }
      }
    });

    // Add navigation control
    map.current.addControl(new maplibregl.NavigationControl(), 'top-right');

    // Add attribution
    map.current.addControl(new maplibregl.AttributionControl({
      customAttribution: '© OpenStreetMap contributors'
    }));

    return () => {
      if (map.current) {
        map.current.remove();
        map.current = null;
      }
    };
  }, []);

  return (
    <div className={`map-container ${className}`}>
      <div ref={mapContainer} className="map" />
    </div>
  );
};

export default Map;