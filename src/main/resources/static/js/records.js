/**
 * records.js — Upgraded Crime Records page with filtering, pagination,
 * CSV export, quick search, and interactive Incident Dossier Modal.
 */

'use strict';

const PAGE_SIZE = 50;
let currentPage = 0;
let currentFilters = {};
let totalPages = 0;
let currentRecordsList = [];
let incidentModalInstance = null;

document.addEventListener('DOMContentLoaded', async () => {
    incidentModalInstance = new bootstrap.Modal(document.getElementById('incidentModal'));

    await loadAreaDropdown(document.getElementById('filterArea'));
    await loadTypeDropdown(document.getElementById('filterType'));
    loadRecords(0, {});

    document.getElementById('applyFilters').addEventListener('click', () => {
        currentFilters = buildFilters();
        loadRecords(0, currentFilters);
    });

    document.getElementById('clearFilters').addEventListener('click', () => {
        document.getElementById('filterKeyword').value  = '';
        document.getElementById('filterArea').value     = '';
        document.getElementById('filterType').value     = '';
        document.getElementById('filterSeverity').value = '';
        document.getElementById('filterFrom').value     = '';
        document.getElementById('filterTo').value       = '';
        currentFilters = {};
        loadRecords(0, {});
    });

    // Quick keyword instant filter
    const keywordInput = document.getElementById('filterKeyword');
    if (keywordInput) {
        let debounceTimer;
        keywordInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                filterCurrentRows(keywordInput.value.trim().toLowerCase());
            }, 250);
        });
    }

    // Export CSV handlers
    const exportCsvBtn = document.getElementById('exportCsvBtn');
    const exportFilteredBtn = document.getElementById('exportFilteredBtn');

    const handleExport = () => {
        const params = new URLSearchParams(buildFilters());
        window.location.href = `/api/crimes/export/csv?${params.toString()}`;
    };

    if (exportCsvBtn) exportCsvBtn.addEventListener('click', handleExport);
    if (exportFilteredBtn) exportFilteredBtn.addEventListener('click', handleExport);
});

function buildFilters() {
    const f = {};
    const area  = document.getElementById('filterArea').value.trim();
    const type  = document.getElementById('filterType').value.trim();
    const sev   = document.getElementById('filterSeverity').value.trim();
    const from  = document.getElementById('filterFrom').value;
    const to    = document.getElementById('filterTo').value;
    if (area) f.area      = area;
    if (type) f.crimeType = type;
    if (sev)  f.severity  = sev;
    if (from) f.fromDate  = from;
    if (to)   f.toDate    = to;
    return f;
}

async function loadRecords(page, filters) {
    const tbody     = document.getElementById('recordsBody');
    const countEl   = document.getElementById('recordCount');
    tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4">
        <div class="spinner-border spinner-border-sm text-primary me-2"></div>Streaming master records…
    </td></tr>`;

    try {
        const params = new URLSearchParams({ page, size: PAGE_SIZE, ...filters });
        const data   = await apiFetch(`/api/crimes/filter/paged?${params}`);

        currentPage = page;
        totalPages  = data.totalPages;
        currentRecordsList = data.content || [];

        countEl.textContent = `${fmt(data.totalElements)} records`;
        document.getElementById('pageInfo').textContent =
            `Page ${data.number + 1} of ${data.totalPages}`;

        renderTableRows(currentRecordsList);
        renderPagination(data.number, data.totalPages);
    } catch (err) {
        console.error('Records load error:', err);
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-danger">
            <i class="bi bi-exclamation-circle me-2"></i>Failed to load records. Is the backend service active?
        </td></tr>`;
    }
}

function renderTableRows(records) {
    const tbody = document.getElementById('recordsBody');
    if (!records || records.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-5 text-muted">
            <i class="bi bi-search me-2"></i>No records match your filters.
        </td></tr>`;
        return;
    }

    tbody.innerHTML = records.map((r, idx) => `
        <tr onclick="openDossier(${idx})">
            <td class="text-muted fw-mono small"><span class="badge bg-secondary-subtle text-body">${r.crimeId}</span></td>
            <td>${fmtDate(r.crimeDate)}</td>
            <td class="fw-mono small">${r.crimeTime ? r.crimeTime.substring(0,5) : '—'}</td>
            <td class="fw-semibold text-primary">${r.area}</td>
            <td>${r.crimeType}</td>
            <td>${severityBadge(r.severity)}</td>
            <td>${statusBadge(r.caseStatus)}</td>
            <td class="text-truncate-2 text-muted small" style="max-width:220px">${r.description || '—'}</td>
            <td class="text-center">
                <button class="btn btn-xs btn-outline-primary py-0 px-2" title="Inspect Incident Dossier">
                    <i class="bi bi-eye"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function filterCurrentRows(keyword) {
    if (!keyword) {
        renderTableRows(currentRecordsList);
        return;
    }
    const filtered = currentRecordsList.filter(r => {
        return (r.crimeId && r.crimeId.toLowerCase().includes(keyword)) ||
               (r.area && r.area.toLowerCase().includes(keyword)) ||
               (r.crimeType && r.crimeType.toLowerCase().includes(keyword)) ||
               (r.description && r.description.toLowerCase().includes(keyword));
    });
    renderTableRows(filtered);
}

function openDossier(idx) {
    const r = currentRecordsList[idx];
    if (!r) return;

    document.getElementById('modalCrimeId').textContent = r.crimeId;
    document.getElementById('modalType').textContent = r.crimeType;
    document.getElementById('modalSeverity').innerHTML = severityBadge(r.severity);
    document.getElementById('modalStatus').innerHTML = statusBadge(r.caseStatus);

    document.getElementById('modalArea').textContent = r.area;
    document.getElementById('modalZone').textContent = r.zone || 'Central Zone';
    document.getElementById('modalCoords').textContent = `${r.latitude ? r.latitude.toFixed(5) : '—'}, ${r.longitude ? r.longitude.toFixed(5) : '—'}`;

    document.getElementById('modalDate').textContent = fmtDate(r.crimeDate);
    document.getElementById('modalTime').textContent = r.crimeTime || '—';
    document.getElementById('modalDay').textContent = r.dayOfWeek || '—';

    document.getElementById('modalDescription').textContent = r.description || 'No detailed narrative provided for this synthetic record.';

    incidentModalInstance.show();
}

function renderPagination(current, total) {
    const ul = document.getElementById('pagination');
    ul.innerHTML = '';
    if (total <= 1) return;

    const maxVisible = 5;
    const half = Math.floor(maxVisible / 2);
    let start = Math.max(0, current - half);
    let end   = Math.min(total - 1, start + maxVisible - 1);
    if (end - start < maxVisible - 1) {
        start = Math.max(0, end - maxVisible + 1);
    }

    const addBtn = (label, page, disabled, active) => {
        const li = document.createElement('li');
        li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
        if (!disabled && !active) {
            li.querySelector('a').addEventListener('click', (e) => {
                e.preventDefault();
                loadRecords(page, currentFilters);
            });
        }
        ul.appendChild(li);
    };

    addBtn('«', 0, current === 0, false);
    addBtn('‹', current - 1, current === 0, false);

    for (let p = start; p <= end; p++) {
        addBtn(p + 1, p, false, p === current);
    }

    addBtn('›', current + 1, current === total - 1, false);
    addBtn('»', total - 1, current === total - 1, false);
}
