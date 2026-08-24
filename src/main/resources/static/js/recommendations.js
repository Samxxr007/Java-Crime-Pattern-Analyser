/**
 * recommendations.js — Recommendation page logic
 * Fetches recommendations from API and renders pattern summary + preventive measures.
 */

'use strict';

document.addEventListener('DOMContentLoaded', async () => {
    await loadAreaDropdown(document.getElementById('recArea'), false);

    document.getElementById('getRecBtn').addEventListener('click', () => {
        const area = document.getElementById('recArea').value;
        const type = document.getElementById('recType').value;
        if (!area || !type) {
            showToast('Please select both an area and a crime type.', 'warning');
            return;
        }
        fetchSingleRecommendation(area, type);
    });

    document.getElementById('allRecsBtn').addEventListener('click', () => {
        const area = document.getElementById('recArea').value;
        if (!area) {
            showToast('Please select an area first.', 'warning');
            return;
        }
        fetchAllRecommendations(area);
    });
});

async function fetchSingleRecommendation(area, crimeType) {
    const resultEl  = document.getElementById('recResult');
    const allEl     = document.getElementById('allRecsResult');
    const emptyEl   = document.getElementById('recEmpty');
    const btn       = document.getElementById('getRecBtn');

    btn.disabled    = true;
    btn.innerHTML   = '<span class="spinner-border spinner-border-sm me-1"></span>Loading…';
    resultEl.style.display  = 'none';
    allEl.style.display     = 'none';
    emptyEl.style.display   = 'none';

    try {
        const rec = await apiFetch(
            `/api/recommendations/${encodeURIComponent(area)}/${encodeURIComponent(crimeType)}`
        );
        renderSingleRec(rec);
        resultEl.style.display = 'block';
        resultEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (err) {
        console.error('Rec error:', err);
        showToast('Error loading recommendation. Please try again.', 'error');
        emptyEl.style.display = 'block';
    } finally {
        btn.disabled  = false;
        btn.innerHTML = '<i class="bi bi-lightbulb me-1"></i>Get Recommendation';
    }
}

function renderSingleRec(rec) {
    document.getElementById('rArea').textContent    = rec.area || '—';
    document.getElementById('rType').textContent    = rec.crimeType || '—';
    document.getElementById('rTotal').textContent   = fmt(rec.totalIncidents) + ' records';
    document.getElementById('rPeak').textContent    = rec.peakTimeWindow || '—';
    document.getElementById('rSummary').textContent = rec.patternSummary || '—';
    document.getElementById('rAdvisory').textContent = rec.generalAdvisory || '—';

    document.getElementById('rSeverity').innerHTML = severityBadge(rec.dominantSeverity);
    document.getElementById('rHotspot').innerHTML  = hotspotBadge(rec.hotspotLevel);
    document.getElementById('rScore').textContent  =
        rec.hotspotScore !== undefined ? (rec.hotspotScore * 100).toFixed(1) + '%' : '—';

    const ul = document.getElementById('rMeasures');
    ul.innerHTML = (rec.preventiveMeasures || [])
        .map(m => `<li>${m}</li>`)
        .join('');
}

async function fetchAllRecommendations(area) {
    const resultEl = document.getElementById('recResult');
    const allEl    = document.getElementById('allRecsResult');
    const emptyEl  = document.getElementById('recEmpty');
    const btn      = document.getElementById('allRecsBtn');

    btn.disabled   = true;
    btn.innerHTML  = '<span class="spinner-border spinner-border-sm me-1"></span>Loading…';
    resultEl.style.display = 'none';
    allEl.style.display    = 'none';
    emptyEl.style.display  = 'none';

    try {
        const recs = await apiFetch(`/api/recommendations/area/${encodeURIComponent(area)}`);
        document.getElementById('allRecsAreaTitle').textContent = area;
        renderAllRecs(recs);
        allEl.style.display = 'block';
        allEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (err) {
        console.error('All recs error:', err);
        showToast('Error loading recommendations.', 'error');
        emptyEl.style.display = 'block';
    } finally {
        btn.disabled  = false;
        btn.innerHTML = '<i class="bi bi-list-ul me-1"></i>All for Area';
    }
}

function renderAllRecs(recs) {
    const container = document.getElementById('allRecsContainer');
    if (!recs || recs.length === 0) {
        container.innerHTML = '<p class="text-muted p-4">No recommendations available.</p>';
        return;
    }

    container.innerHTML = recs.map(rec => `
        <div class="all-rec-item">
            <div class="d-flex align-items-center justify-content-between mb-2">
                <div>
                    <span class="rec-type">${rec.crimeType}</span>
                    <span class="rec-count ms-3 text-muted">${fmt(rec.totalIncidents)} records · Peak: ${rec.peakTimeWindow}</span>
                </div>
                <div class="d-flex gap-2 align-items-center">
                    ${severityBadge(rec.dominantSeverity)}
                    ${hotspotBadge(rec.hotspotLevel)}
                </div>
            </div>
            <ul class="rec-measures mb-0" style="columns:2;column-gap:2rem;">
                ${(rec.preventiveMeasures || []).map(m => `<li>${m}</li>`).join('')}
            </ul>
        </div>
    `).join('');
}
