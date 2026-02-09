import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './css/Map.css';

// Map component using Leaflet
// Props:
// - reports: array of { id, latitude, longitude, area, description, status, userName, statusLabel }
// - onReportClick: function(reportId)
// - className
const Map = ({ reports = [], onReportClick = () => { }, className = '' }) => {
  const mapRef = useRef(null);
  const containerRef = useRef(null);
  const markersLayerRef = useRef(null);

  useEffect(() => {
    if (mapRef.current) return;

    const center = [-18.8792, 47.5079];

    // create container for leaflet map inside this component
    const container = containerRef.current;
    mapRef.current = L.map(container, { center, zoom: 12 });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(mapRef.current);

    markersLayerRef.current = L.layerGroup().addTo(mapRef.current);

    return () => {
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
      }
    };
  }, []);

  // ResizeObserver to invalidate Leaflet size when container changes (panel open/close)
  useEffect(() => {
    const map = mapRef.current;
    const container = containerRef.current;
    if (!map || !container || typeof ResizeObserver === 'undefined') return;

    const ro = new ResizeObserver(() => {
      try {
        map.invalidateSize();
      } catch (e) {
        // ignore
      }
    });
    ro.observe(container);

    // also trigger once after a short delay to ensure correct sizing
    const t = setTimeout(() => { try { map.invalidateSize(); } catch (e) { } }, 150);

    return () => {
      ro.disconnect();
      clearTimeout(t);
    };
  }, []);

  // update markers when reports change
  useEffect(() => {
    const map = mapRef.current;
    const markersLayer = markersLayerRef.current;
    if (!map || !markersLayer) return;

    markersLayer.clearLayers();

    const statusToColor = (status) => {
      switch ((status || '').toString()) {
        case 'SUBMITTED': return '#1f78b4';
        case 'ASSIGNED': return '#ff7f00';
        case 'IN_PROGRESS': return '#f1c40f';
        case 'COMPLETED': return '#2ecc71';
        case 'VERIFIED': return '#9b59b6';
        default: return '#7f8c8d';
      }
    };

    (reports || []).forEach((r) => {
      const lon = Number(r.longitude);
      const lat = Number(r.latitude);
      if (!isFinite(lon) || !isFinite(lat)) {
        return;
      }

      const status = (
        (r.status && (r.status.statusCode || r.status.status_code || r.status.code)) || r.status || r.status_code || r.status_id || r.statusCode || 'UNKNOWN'
      );
      const statusLabel = (
        (r.status && (r.status.label || r.status.name)) || r.statusLabel || r.status_label || r.statusName || r.status_name || ''
      );
      const userName = (
        (r.user && (r.user.name || r.user.email)) || r.userName || r.user_name || (r.report && (r.report.user?.name || r.report.user?.email)) || ''
      );
      const description = r.description || r.report?.description || '';

      const pinColor = statusToColor(status);

      // Create a custom SVG pin icon
      const icon = L.divIcon({
        className: 'custom-pin',
        html: `
          <svg width="30" height="42" viewBox="0 0 30 42" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 0C6.71573 0 0 6.71573 0 15C0 26.25 15 42 15 42C15 42 30 26.25 30 15C30 6.71573 23.2843 0 15 0ZM15 20.25C12.1005 20.25 9.75 17.8995 9.75 15C9.75 12.1005 12.1005 9.75 15 9.75C17.8995 9.75 20.25 12.1005 20.25 15C20.25 17.8995 17.8995 20.25 15 20.25Z" fill="${pinColor}" stroke="white" stroke-width="2"/>
          </svg>
        `,
        iconSize: [30, 42],
        iconAnchor: [15, 42],
        popupAnchor: [0, -42]
      });

      const marker = L.marker([lat, lon], { icon });

      const popupHtml = `
        <div style="font-size:12px">
          <strong>${statusLabel || status}</strong><br/>
          ${userName ? userName + '<br/>' : ''}
          Surface: ${r.area || '-'} m²<br/>
          ${(description || '').slice(0, 120)}
          <div style="margin-top:8px;text-align:right">
          <button class="view-images-btn" data-report-id="${r.id}" style="cursor:pointer;padding:6px 8px;border-radius:4px">
            Voir images
          </button>
          </div>
        </div>
      `;

      marker.bindPopup(popupHtml, { className: 'report-popup', autoClose: false, closeOnClick: false, closeButton: true, maxWidth: 240 });

      let popupHovering = false;

      marker.on('mouseover', () => {
        marker.openPopup();
        if (map.getContainer) map.getContainer().style.cursor = 'pointer';
      });

      marker.on('mouseout', () => {
        setTimeout(() => {
          if (!popupHovering) {
            marker.closePopup();
            if (map.getContainer) map.getContainer().style.cursor = '';
          }
        }, 150);
      });

      marker.on('popupopen', () => {
        const popupEl = marker.getPopup().getElement();
        if (!popupEl) return;
        const onEnter = () => { popupHovering = true; };
        const onLeave = () => { popupHovering = false; marker.closePopup(); if (map.getContainer) map.getContainer().style.cursor = ''; };
        popupEl.addEventListener('mouseenter', onEnter);
        popupEl.addEventListener('mouseleave', onLeave);

        const btn = popupEl.querySelector('.view-images-btn');
        if (btn) {
          btn.addEventListener('click', (ev) => {
            ev.stopPropagation();
            marker.closePopup();
            onReportClick(r.id, { focusImages: true });
          });
        }

        marker.once('popupclose', () => {
          try {
            popupEl.removeEventListener('mouseenter', onEnter);
            popupEl.removeEventListener('mouseleave', onLeave);
          } catch (e) {
          }
        });
      });

      marker.on('click', () => onReportClick(r.id));

      markersLayer.addLayer(marker);
    });
  }, [reports, onReportClick]);

  return (
    <div className={`map-container ${className}`}>
      <div ref={containerRef} className="map" />
    </div>
  );
};

export default Map;