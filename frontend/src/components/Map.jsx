import { useEffect, useRef, useState } from 'react';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import './Map.css';

// Map component renders a MapLibre map and a point layer from `reports` prop.
// Props:
// - reports: array of { id, latitude, longitude, area, description, status, userName, statusLabel }
// - onReportClick: function(reportId)
// - className
const Map = ({ reports = [], onReportClick = () => {}, className = '' }) => {
  const mapContainer = useRef(null);
  const map = useRef(null);
  const popupRef = useRef(null);
  const [mapError, setMapError] = useState(null);

  useEffect(() => {
    if (map.current) return; // initialize map only once

    map.current = new maplibregl.Map({
      container: mapContainer.current,
      style: '/styles/osm-bright/style.json', // tileserver style via nginx proxy
      center: [47.519, -18.879], // center of Antananarivo
      zoom: 12,
      attributionControl: false,
      transformRequest: (url, resourceType) => {
        if (url.startsWith('/')) {
          return { url: `${window.location.origin}${url}` };
        }
      }
    });

    map.current.on('error', (e) => {
      console.error('[Map] Error loading map:', e);
      if (e.error && e.error.message) {
        console.error('[Map] Error details:', e.error.message);
        if (e.error.message.includes('Unexpected token') || e.error.message.includes('JSON')) {
          setMapError('Le serveur de tuiles (tileserver) ne répond pas correctement. Vérifiez que le service tileserver est démarré.');
        } else {
          setMapError(`Erreur de chargement de la carte: ${e.error.message}`);
        }
      }
    });

    map.current.addControl(new maplibregl.NavigationControl(), 'top-right');
    map.current.addControl(new maplibregl.AttributionControl({ customAttribution: '© OpenStreetMap contributors' }));

    // create popup
    popupRef.current = new maplibregl.Popup({ closeButton: false, closeOnClick: false });

    // NOTE: background click handler moved into addReportsSourceAndLayers()

    return () => {
      if (map.current) {
        map.current.remove();
        map.current = null;
      }
    };
  }, []);

  // update source/layers when reports change
  useEffect(() => {
    if (!map.current) return;

    const mapInstance = map.current;

    const features = [];
    (reports || []).forEach((r) => {
      const lon = Number(r.longitude);
      const lat = Number(r.latitude);
      if (!isFinite(lon) || !isFinite(lat)) {
        // skip reports without valid coordinates to avoid breaking the GeoJSON source
        console.warn('[Map] skipping report without valid coordinates', r?.id, r?.longitude, r?.latitude);
        return;
      }

      features.push({
        type: 'Feature',
        geometry: { type: 'Point', coordinates: [lon, lat] },
        properties: {
          id: r.id,
          area: r.area,
          description: r.description || r.report?.description || '',
          // Normalize status from various API shapes: object or primitive
          status: (
            (r.status && (r.status.statusCode || r.status.status_code || r.status.code)) || r.status || r.status_code || r.status_id || r.statusCode || 'UNKNOWN'
          ),
          // Preferred human label from nested object
          statusLabel: (
            (r.status && (r.status.label || r.status.name)) || r.statusLabel || r.status_label || r.statusName || r.status_name || ''
          ),
          // Prefer user.name then email, support nested user object
          userName: (
            (r.user && (r.user.name || r.user.email)) || r.userName || r.user_name || (r.report && (r.report.user?.name || r.report.user?.email)) || ''
          )
        }
      });
    });

    const sourceData = { type: 'FeatureCollection', features };

    const addReportsSourceAndLayers = () => {
      if (!mapInstance || !mapInstance.style) return;
      if (mapInstance.getSource('reports')) {
        try { mapInstance.getSource('reports').setData(sourceData); } catch (err) { console.warn('setData failed', err); }
        return;
      }

      mapInstance.addSource('reports', { type: 'geojson', data: sourceData });

      // circle layer colored by status (large constant pixel radius so markers stay visible at all zooms)
      mapInstance.addLayer({
        id: 'reports-circle',
        type: 'circle',
        source: 'reports',
        paint: {
          // constant pixel radius (visible when zoomed out or in)
          'circle-radius': 14,
          // slightly larger on high-DPI screens - use circle-pitch-scale viewport so size is in pixels
          'circle-pitch-scale': 'viewport',
          'circle-color': [
            'match', ['get', 'status'],
            'SUBMITTED', '#1f78b4',
            'ASSIGNED', '#ff7f00',
            'IN_PROGRESS', '#ffd92f',
            'COMPLETED', '#33a02c',
            'VERIFIED', '#6a3d9a',
            /* default */ '#666666'
          ],
          'circle-stroke-color': '#ffffff',
          'circle-stroke-width': 2,
          'circle-opacity': 0.95
        }
      });

      // small text label with status short code
      mapInstance.addLayer({
        id: 'reports-label',
        type: 'symbol',
        source: 'reports',
        layout: {
          'text-field': ['get', 'status'],
          'text-font': ['NotoSansRegular'],
          'text-size': 10,
          'text-offset': [0, 1.2]
        },
        paint: {
          'text-color': '#222',
          'text-halo-color': 'rgba(255,255,255,0.9)',
          'text-halo-width': 1
        }
      });

      // clear selection when clicking on map background: only after layers exist
      mapInstance.on('click', (e) => {
        try {
          const features = mapInstance.queryRenderedFeatures(e.point, { layers: ['reports-circle'] });
          if (!features || features.length === 0) {
            onReportClick(null);
          }
        } catch (err) {
          // defensive: ignore if query fails
           console.warn('[Map] queryRenderedFeatures failed on background click', err?.message || err);
        }
      });

      // popup handlers
      mapInstance.on('mousemove', 'reports-circle', (e) => {
        mapInstance.getCanvas().style.cursor = 'pointer';
        const feat = e.features[0];
        const props = feat.properties || {};
        const coords = feat.geometry && feat.geometry.coordinates ? feat.geometry.coordinates : [0,0];
        const html = `<div style="font-size:12px"><strong>${props.statusLabel || props.status}</strong><br/>${props.userName ? props.userName + '<br/>' : ''}Surface: ${props.area || '-'} m²<br/>${(props.description || '').slice(0,120)}</div>`;
        popupRef.current.setLngLat(coords).setHTML(html).addTo(mapInstance);
      });

      mapInstance.on('mouseleave', 'reports-circle', () => {
        mapInstance.getCanvas().style.cursor = '';
        popupRef.current.remove();
      });

      mapInstance.on('click', 'reports-circle', (e) => {
        const feat = e.features[0];
        const id = feat.properties?.id;
        onReportClick(id);
      });
    };

    // If style is ready, add immediately. Otherwise wait for styledata/load event.
    let onStyleData = null;
    if (mapInstance.isStyleLoaded && mapInstance.isStyleLoaded()) {
      addReportsSourceAndLayers();
    } else {
      onStyleData = () => {
        if (mapInstance.isStyleLoaded && mapInstance.isStyleLoaded()) {
          addReportsSourceAndLayers();
          mapInstance.off('styledata', onStyleData);
        }
      };
      mapInstance.on('styledata', onStyleData);
    }

    // cleanup: remove handler if reports effect re-runs
    return () => {
      if (onStyleData && mapInstance && mapInstance.off) {
        mapInstance.off('styledata', onStyleData);
      }
    };
  }, [reports, onReportClick]);

  return (
    <div className={`map-container ${className}`}>
      {mapError && (
        <div style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          background: '#fff',
          padding: '20px',
          borderRadius: '8px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
          zIndex: 1000,
          maxWidth: '400px',
          textAlign: 'center'
        }}>
          <h3 style={{ color: '#e74c3c', marginBottom: '10px' }}>Erreur de carte</h3>
          <p>{mapError}</p>
          <p style={{ fontSize: '12px', color: '#666', marginTop: '10px' }}>
            Vérifiez que tous les services Docker sont démarrés (notamment le tileserver).
          </p>
        </div>
      )}
      <div ref={mapContainer} className="map" />
    </div>
  );
};

export default Map;