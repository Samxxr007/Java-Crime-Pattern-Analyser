/**
 * recommendations.js — Upgraded CPTED Prevention Engine
 * Displays categorized operational and infrastructural recommendations.
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
    btn.innerHTML   = '<span class="spinner-border spinner-border-sm me-1"></span>Evaluating…';
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
        btn.innerHTML = '<i class="bi bi-lightbulb me-1"></i>Analyze';
    }
}

function renderSingleRec(rec) {
    document.getElementById('rArea').textContent    = rec.area || '—';
    document.getElementById('rType').textContent    = rec.crimeType || '—';
    document.getElementById('rTotal').textContent   = fmt(rec.totalIncidents) + ' incidents';
    document.getElementById('rPeak').textContent    = rec.peakTimeWindow || '—';
    document.getElementById('rSummary').textContent = rec.patternSummary || '—';
    document.getElementById('rAdvisory').textContent = rec.generalAdvisory || '—';

    document.getElementById('rSeverity').innerHTML = severityBadge(rec.dominantSeverity);
    document.getElementById('rHotspot').innerHTML  = hotspotBadge(rec.hotspotLevel);
    document.getElementById('rScore').textContent  =
        rec.hotspotScore !== undefined ? rec.hotspotScore.toFixed(3) : '—';

    // Safety circle
    const hScore = rec.hotspotScore || 0.4;
    const safety = Math.max(25, Math.min(95, Math.round(100 - (hScore * 65))));
    document.getElementById('recSafetyScore').textContent = safety;
    const circle = document.getElementById('recSafetyCircle');
    circle.className = `score-circle ${safety >= 75 ? 'safe' : safety >= 50 ? 'moderate' : 'risk'} mb-3`;

    const measures = rec.preventiveMeasures || [];
    const imm = measures.slice(0, 2);
    const med = measures.slice(2, 4);
    const lng = measures.length > 4 ? measures.slice(4) : [
        "Coordinate with Greater Chennai Police community liaison for neighborhood watch deployment.",
        "Implement civic surveillance awareness programs and smart emergency call-box installation."
    ];

    document.getElementById('rMeasuresImmediate').innerHTML = imm.map(m => `<li>${m}</li>`).join('') || '<li>Standard patrol frequency maintained.</li>';
    document.getElementById('rMeasuresMedium').innerHTML = med.map(m => `<li>${m}</li>`).join('') || '<li>Evaluate lighting and visibility along transit paths.</li>';
    document.getElementById('rMeasuresLong').innerHTML = lng.map(m => `<li>${m}</li>`).join('');
}

async function fetchAllRecommendations(area) {
    const resultEl = document.getElementById('recResult');
    const allEl    = document.getElementById('allRecsResult');
    const emptyEl  = document.getElementById('recEmpty');
    const btn      = document.getElementById('allRecsBtn');

    btn.disabled   = true;
    btn.innerHTML  = '<span class="spinner-border spinner-border-sm me-1"></span>Compiling…';
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
        btn.innerHTML = '<i class="bi bi-list-ul me-1"></i>All 10 Types';
    }
}

function renderAllRecs(recs) {
    const container = document.getElementById('allRecsContainer');
    if (!recs || recs.length === 0) {
        container.innerHTML = '<div class="p-4 text-center text-muted">No recommendations available.</div>';
        return;
    }

    container.innerHTML = `
        <div class="accordion accordion-flush" id="allRecsAccordion">
            ${recs.map((r, i) => `
                <div class="accordion-item">
                    <h2 class="accordion-header" id="heading${i}">
                        <button class="accordion-button ${i !== 0 ? 'collapsed' : ''}" type="button"
                                data-bs-toggle="collapse" data-bs-target="#collapse${i}">
                            <div class="d-flex align-items-center gap-3 w-100 me-3">
                                <span class="fw-bold">${r.crimeType}</span>
                                <span class="badge bg-secondary-subtle text-body ms-auto me-2">${r.totalIncidents} incidents</span>
                                ${severityBadge(r.dominantSeverity)}
                                ${hotspotBadge(r.hotspotLevel)}
                            </div>
                        </button>
                    </h2>
                    <div id="collapse${i}" class="accordion-collapse collapse ${i === 0 ? 'show' : ''}"
                         data-bs-parent="#allRecsAccordion">
                        <div class="accordion-body">
                            <p class="text-muted small mb-2 font-monospace">${r.patternSummary || ''}</p>
                            <h6 class="fw-semibold mb-2 text-primary">Intervention Directives:</h6>
                            <ul class="rec-measures mb-3">
                                ${(r.preventiveMeasures || []).map(m => `<li>${m}</li>`).join('')}
                            </ul>
                            <div class="p-2 border rounded bg-body-tertiary small text-secondary">
                                <strong>Advisory:</strong> ${r.generalAdvisory || ''}
                            </div>
                        </div>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}
