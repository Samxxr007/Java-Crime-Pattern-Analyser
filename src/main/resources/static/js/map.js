/**
 * map.js — Leaflet.js map with marker clustering and density heatmap
 * Chennai Crime Pattern Analyzer — Synthetic Data Only
 */

'use strict';

let map, markerClusterGroup, heatLayer;
const MAX_MAP_RECORDS = 1000;

// Marker colours by severity
const SEVERITY_COLOR = {
    HIGH:   '#ef4444',
    MEDIUM: '#f97316',
    LOW:    '#22c55e'
};

document.addEventListener('DOMContentLoaded', async () => {
    initMap();
    await loadAreaDropdown(document.getElementById('mapFilterArea'));
    await loadTypeDropdown(document.getElementById('mapFilterType'));
    await loadMapData({});

    document.getElementById('applyMapFilters').addEventListener('click', () => {
        const filters = buildMapFilters();
        loadMapData(filters);
    });

    document.getElementById('clearMapFilters').addEventListener('click', () => {
        document.getElementById('mapFilterArea').value     = '';
        document.getElementById('mapFilterType').value     = '';
        document.getElementById('mapFilterSeverity').value = '';
        document.getElementById('mapViewMode').value       = 'markers';
        loadMapData({});
    });

    document.getElementById('mapViewMode').addEventListener('change', () => {
        const filters = buildMapFilters();
        loadMapData(filters);
    });
});

function initMap() {
    map = L.map('map', {
        center: [13.0827, 80.2707],  // Chennai city centre
        zoom: 11,
        zoomControl: true,
        attributionControl: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© <a href="https://openstreetmap.org">OpenStreetMap</a> contributors',
        maxZoom: 18
    }).addTo(map);

    markerClusterGroup = L.markerClusterGroup({
        chunkedLoading: true,
        maxClusterRadius: 50,
        iconCreateFunction: cluster => {
            const count = cluster.getChildCount();
            const cls = count > 50 ? 'large' : count > 20 ? 'medium' : 'small';
            return L.divIcon({
                html: `<div class="cluster-icon cluster-${cls}"><span>${count}</span></div>`,
                className: '',
                iconSize: [40, 40]
            });
        }
    });
}

function buildMapFilters() {
    const f = {};
    const area = document.getElementById('mapFilterArea').value;
    const type = document.getElementById('mapFilterType').value;
    const sev  = document.getElementById('mapFilterSeverity').value;
    if (area) f.area      = area;
    if (type) f.crimeType = type;
    if (sev)  f.severity  = sev;
    return f;
}

async function loadMapData(filters) {
    const countEl = document.getElementById('mapRecordCount');
    countEl.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Loading…';

    // Clear existing layers
    if (markerClusterGroup) {
        markerClusterGroup.clearLayers();
        map.removeLayer(markerClusterGroup);
    }
    if (heatLayer) {
        map.removeLayer(heatLayer);
        heatLayer = null;
    }

    try {
        const params = new URLSearchParams({ ...filters, size: MAX_MAP_RECORDS });
        const data   = await apiFetch(`/api/crimes/filter?${params}`);

        const viewMode = document.getElementById('mapViewMode').value;
        countEl.innerHTML = `<i class="bi bi-pin-map me-1"></i>${fmt(data.length)} markers displayed`;

        if (viewMode === 'heatmap') {
            renderHeatmap(data);
        } else {
            renderMarkers(data);
        }
    } catch (err) {
        console.error('Map data error:', err);
        countEl.innerHTML = '<i class="bi bi-exclamation-circle text-danger me-1"></i>Error loading data';
    }
}

function renderMarkers(records) {
    markerClusterGroup.clearLayers();

    records.forEach(r => {
        if (!r.latitude || !r.longitude) return;

        const color = SEVERITY_COLOR[r.severity] || '#64748b';
        const icon  = L.divIcon({
            html: `<div style="
                width:12px;height:12px;
                background:${color};
                border:2px solid #fff;
                border-radius:50%;
                box-shadow:0 0 4px rgba(0,0,0,.3);
            "></div>`,
            className: '',
            iconSize: [14, 14],
            iconAnchor: [7, 7]
        });

        const marker = L.marker([r.latitude, r.longitude], { icon });
        marker.bindPopup(`
            <div class="popup-header">${r.crimeType}</div>
            <div class="popup-row"><span class="popup-label">Area</span><span><b>${r.area}</b></span></div>
            <div class="popup-row"><span class="popup-label">Date</span><span>${fmtDate(r.crimeDate)}</span></div>
            <div class="popup-row"><span class="popup-label">Time</span><span>${r.crimeTime ? r.crimeTime.substring(0,5) : '—'}</span></div>
            <div class="popup-row"><span class="popup-label">Severity</span><span>${severityBadge(r.severity)}</span></div>
            <div class="popup-row"><span class="popup-label">Status</span><span>${statusBadge(r.caseStatus)}</span></div>
            <div class="mt-2 text-muted" style="font-size:11px">${r.description || ''}</div>
            <div class="mt-2" style="font-size:10px;color:#94a3b8">Synthetic data — academic use only</div>
        `, { maxWidth: 220 });

        markerClusterGroup.addLayer(marker);
    });

    map.addLayer(markerClusterGroup);
}

function renderHeatmap(records) {
    const points = records
        .filter(r => r.latitude && r.longitude)
        .map(r => {
            const weight = r.severity === 'HIGH' ? 1.0 : r.severity === 'MEDIUM' ? 0.6 : 0.3;
            return [r.latitude, r.longitude, weight];
        });

    heatLayer = L.heatLayer(points, {
        radius:   28,
        blur:     20,
        maxZoom:  14,
        gradient: { 0.2: '#22c55e', 0.5: '#f97316', 0.8: '#ef4444', 1.0: '#7f1d1d' }
    });
    heatLayer.addTo(map);
}

// -------------------------------------------------------
// Cluster icon CSS (injected dynamically)
// -------------------------------------------------------
(function injectClusterStyles() {
    const style = document.createElement('style');
    style.textContent = `
        .cluster-icon {
            display:flex;align-items:center;justify-content:center;
            border-radius:50%;color:#fff;font-weight:700;font-size:12px;
            border:2px solid #fff;box-shadow:0 2px 8px rgba(0,0,0,.25);
        }
        .cluster-small  { width:32px;height:32px;background:#3b82f6; }
        .cluster-medium { width:38px;height:38px;background:#f97316; }
        .cluster-large  { width:44px;height:44px;background:#ef4444; }
    `;
    document.head.appendChild(style);
})();
