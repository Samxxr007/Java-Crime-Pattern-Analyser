/**
 * map.js — Professional Command Center Geospatial Mapping
 * Multi-layer basemaps, marker clustering, density heatmaps,
 * spatial radius proximity scanner, and patrol waypoint routing.
 */

'use strict';

let map, markerClusterGroup, heatLayer, scanCircle, patrolLayerGroup;
const MAX_MAP_RECORDS = 1000;
let currentBaseTileLayer = null;
let currentRadiusKm = 1;
let isPatrolVisible = false;

// Tile layers definitions (100% Free, Zero API Keys, Zero Watermarks)
const TILE_PROVIDERS = {
    dark: {
        url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        attr: '© <a href="https://openstreetmap.org">OpenStreetMap</a> contributors',
        className: 'dark-tiles'
    },
    osm: {
        url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        attr: '© <a href="https://openstreetmap.org">OpenStreetMap</a> contributors',
        className: 'standard-tiles'
    },
    satellite: {
        url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
        attr: '© <a href="https://www.esri.com/">Esri World Imagery</a>',
        className: 'satellite-tiles'
    }
};

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
        if (scanCircle) { map.removeLayer(scanCircle); scanCircle = null; }
        if (patrolLayerGroup) { map.removeLayer(patrolLayerGroup); isPatrolVisible = false; }
        document.getElementById('proximityAlert').classList.add('d-none');
        loadMapData({});
    });

    document.getElementById('mapViewMode').addEventListener('change', () => {
        const filters = buildMapFilters();
        loadMapData(filters);
    });

    document.getElementById('mapBaseLayer').addEventListener('change', (e) => {
        switchBaseLayer(e.target.value);
    });

    // Map click for spatial radius scanning
    map.on('click', (e) => {
        triggerProximityScan(e.latlng.lat, e.latlng.lng);
    });

    // Synchronize base map layer with theme mode
    window.addEventListener('themeChanged', (e) => {
        const isDark = e.detail?.isDark;
        const baseLayerSelect = document.getElementById('mapBaseLayer');
        const newLayer = isDark ? 'dark' : 'osm';
        if (baseLayerSelect) baseLayerSelect.value = newLayer;
        switchBaseLayer(newLayer);
    });
});

function initMap() {
    map = L.map('map', {
        center: [13.0418, 80.2341],  // Chennai central coordinates
        zoom: 12,
        zoomControl: true,
        attributionControl: true
    });

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const initialLayer = isDark ? 'dark' : 'osm';
    const baseLayerSelect = document.getElementById('mapBaseLayer');
    if (baseLayerSelect) baseLayerSelect.value = initialLayer;
    switchBaseLayer(initialLayer);

    markerClusterGroup = L.markerClusterGroup({
        chunkedLoading: true,
        maxClusterRadius: 45,
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

    patrolLayerGroup = L.layerGroup();
}

function switchBaseLayer(key) {
    if (currentBaseTileLayer) {
        map.removeLayer(currentBaseTileLayer);
    }
    const provider = TILE_PROVIDERS[key] || TILE_PROVIDERS.dark;
    currentBaseTileLayer = L.tileLayer(provider.url, {
        attribution: provider.attr,
        className: provider.className || '',
        maxZoom: 18
    }).addTo(map);
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
    countEl.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Streaming telemetry…';

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
        const valid    = data.filter(r => r.latitude && r.longitude);

        countEl.innerHTML = `<i class="bi bi-pin-map-fill me-1"></i>${fmt(valid.length)} plotted`;

        if (viewMode === 'heatmap') {
            renderHeatmap(valid);
        } else {
            renderMarkers(valid);
        }

        // Center view on filtered area if chosen
        if (filters.area && valid.length > 0) {
            map.setView([valid[0].latitude, valid[0].longitude], 14);
        }
    } catch (err) {
        console.error('Map data load error:', err);
        countEl.innerHTML = '<span class="text-danger"><i class="bi bi-exclamation-circle me-1"></i>Load error</span>';
    }
}

function renderMarkers(records) {
    records.forEach(r => {
        const color  = SEVERITY_COLOR[r.severity] || '#6c757d';
        const marker = L.circleMarker([r.latitude, r.longitude], {
            radius: 7,
            fillColor: color,
            color: '#ffffff',
            weight: 1.5,
            opacity: 0.9,
            fillOpacity: 0.85
        });

        marker.bindPopup(`
            <div class="popup-header">
                <span class="badge bg-secondary-subtle text-body me-1">${r.crimeId}</span>
                ${r.crimeType}
            </div>
            <div class="popup-row">
                <span class="popup-label">Area:</span>
                <span class="fw-semibold">${r.area}</span>
            </div>
            <div class="popup-row">
                <span class="popup-label">Date &amp; Time:</span>
                <span>${fmtDate(r.crimeDate)} ${r.crimeTime ? r.crimeTime.substring(0,5) : ''}</span>
            </div>
            <div class="popup-row">
                <span class="popup-label">Severity:</span>
                <span>${severityBadge(r.severity)}</span>
            </div>
            <div class="popup-row">
                <span class="popup-label">Status:</span>
                <span>${statusBadge(r.caseStatus)}</span>
            </div>
            ${r.description ? `<div class="mt-2 text-muted small border-top pt-1 font-monospace">${r.description}</div>` : ''}
        `);

        markerClusterGroup.addLayer(marker);
    });

    map.addLayer(markerClusterGroup);
}

function renderHeatmap(records) {
    const points = records.map(r => {
        const weight = r.severity === 'HIGH' ? 1.0 : r.severity === 'MEDIUM' ? 0.6 : 0.3;
        return [r.latitude, r.longitude, weight];
    });

    heatLayer = L.heatLayer(points, {
        radius: 26,
        blur: 18,
        maxZoom: 16,
        max: 1.0,
        gradient: {
            0.2: '#3b82f6',
            0.4: '#10b981',
            0.6: '#f59e0b',
            0.8: '#f97316',
            1.0: '#ef4444'
        }
    }).addTo(map);
}

// -------------------------------------------------------
// Spatial Radius Proximity Scanner
// -------------------------------------------------------
function setScanRadius(km) {
    currentRadiusKm = km;
    document.getElementById('btnRadius1').classList.toggle('active', km === 1);
    document.getElementById('btnRadius3').classList.toggle('active', km === 3);
    document.getElementById('btnRadius5').classList.toggle('active', km === 5);

    if (scanCircle) {
        const center = scanCircle.getLatLng();
        triggerProximityScan(center.lat, center.lng);
    }
}

async function triggerProximityScan(lat, lon) {
    if (scanCircle) {
        map.removeLayer(scanCircle);
    }

    scanCircle = L.circle([lat, lon], {
        radius: currentRadiusKm * 1000,
        color: '#f59e0b',
        fillColor: '#f59e0b',
        fillOpacity: 0.15,
        weight: 2,
        dashArray: '5, 5'
    }).addTo(map);

    const alertEl = document.getElementById('proximityAlert');
    const alertText = document.getElementById('proximityAlertText');
    alertEl.classList.remove('d-none');
    alertText.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Evaluating spatial radius (${currentRadiusKm} km) around [${lat.toFixed(4)}, ${lon.toFixed(4)}]…`;

    try {
        const data = await apiFetch(`/api/analytics/proximity?lat=${lat}&lon=${lon}&radiusKm=${currentRadiusKm}`);

        const topType = Object.keys(data.crimeTypeBreakdown || {})[0] || 'None';
        alertText.innerHTML = `<strong>Spatial Proximity Assessment:</strong> Found <strong>${fmt(data.totalIncidents)} incidents</strong> within <strong>${currentRadiusKm} km</strong> buffer (Average Distance: <strong>${data.averageDistanceKm} km</strong>). Primary Risk Vector: <span class="badge bg-danger-subtle text-danger">${topType}</span>. Closest Locality: <strong>${data.nearestIncidentArea}</strong>.`;
    } catch (err) {
        console.error('Proximity scan failed:', err);
        alertText.textContent = 'Proximity scan query failed.';
    }
}

// -------------------------------------------------------
// Suggested Patrol Waypoint Route
// -------------------------------------------------------
async function togglePatrolRoute() {
    if (isPatrolVisible) {
        map.removeLayer(patrolLayerGroup);
        patrolLayerGroup.clearLayers();
        isPatrolVisible = false;
        document.getElementById('btnPatrolRoute').classList.remove('active');
        return;
    }

    try {
        const hotspots = await apiFetch('/api/analytics/hotspots');
        const top5 = hotspots.slice(0, 5);

        const latlngs = top5.map(h => [h.latitude, h.longitude]);

        // Draw connecting polyline
        const polyline = L.polyline(latlngs, {
            color: '#38bdf8',
            weight: 3,
            dashArray: '8, 8',
            opacity: 0.8
        });

        patrolLayerGroup.addLayer(polyline);

        top5.forEach((h, idx) => {
            const marker = L.circleMarker([h.latitude, h.longitude], {
                radius: 10,
                fillColor: '#38bdf8',
                color: '#ffffff',
                weight: 2,
                fillOpacity: 0.9
            });
            marker.bindPopup(`<strong>Patrol Checkpoint #${idx + 1}</strong><br>${h.area}<br>Hotspot Score: ${h.hotspotScore.toFixed(3)} (${h.level} RISK)`);
            patrolLayerGroup.addLayer(marker);
        });

        patrolLayerGroup.addTo(map);
        isPatrolVisible = true;
        document.getElementById('btnPatrolRoute').classList.add('active');
    } catch (err) {
        console.error('Failed to generate patrol route:', err);
    }
}
