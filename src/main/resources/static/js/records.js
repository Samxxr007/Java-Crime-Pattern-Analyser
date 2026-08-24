/**
 * records.js — Crime Records page with filtering, pagination, and table rendering
 */

'use strict';

const PAGE_SIZE = 50;
let currentPage = 0;
let currentFilters = {};
let totalPages = 0;

document.addEventListener('DOMContentLoaded', async () => {
    await loadAreaDropdown(document.getElementById('filterArea'));
    await loadTypeDropdown(document.getElementById('filterType'));
    loadRecords(0, {});

    document.getElementById('applyFilters').addEventListener('click', () => {
        currentFilters = buildFilters();
        loadRecords(0, currentFilters);
    });

    document.getElementById('clearFilters').addEventListener('click', () => {
        document.getElementById('filterArea').value     = '';
        document.getElementById('filterType').value     = '';
        document.getElementById('filterSeverity').value = '';
        document.getElementById('filterFrom').value     = '';
        document.getElementById('filterTo').value       = '';
        currentFilters = {};
        loadRecords(0, {});
    });
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
    tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4">
        <div class="spinner-border spinner-border-sm text-primary me-2"></div>Loading…
    </td></tr>`;

    try {
        const params = new URLSearchParams({ page, size: PAGE_SIZE, ...filters });
        const data   = await apiFetch(`/api/crimes/filter/paged?${params}`);

        currentPage = page;
        totalPages  = data.totalPages;

        countEl.textContent = `${fmt(data.totalElements)} records`;
        document.getElementById('pageInfo').textContent =
            `Page ${data.number + 1} of ${data.totalPages}`;

        if (data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-5 text-muted">
                <i class="bi bi-search me-2"></i>No records match your filters.
            </td></tr>`;
        } else {
            tbody.innerHTML = data.content.map(r => `
                <tr>
                    <td class="text-muted fw-mono small">${r.crimeId}</td>
                    <td>${fmtDate(r.crimeDate)}</td>
                    <td class="fw-mono small">${r.crimeTime ? r.crimeTime.substring(0,5) : '—'}</td>
                    <td class="fw-semibold">${r.area}</td>
                    <td>${r.crimeType}</td>
                    <td>${severityBadge(r.severity)}</td>
                    <td>${statusBadge(r.caseStatus)}</td>
                    <td class="text-truncate-2 text-muted small" style="max-width:200px">${r.description || '—'}</td>
                </tr>
            `).join('');
        }

        renderPagination(data.number, data.totalPages);
    } catch (err) {
        console.error('Records load error:', err);
        tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-danger">
            <i class="bi bi-exclamation-circle me-2"></i>Failed to load records.
        </td></tr>`;
    }
}

function renderPagination(current, total) {
    const ul = document.getElementById('pagination');
    ul.innerHTML = '';
    if (total <= 1) return;

    const maxVisible = 5;
    const half = Math.floor(maxVisible / 2);
    let start = Math.max(0, current - half);
    let end   = Math.min(total - 1, start + maxVisible - 1);
    if (end - start < maxVisible - 1) start = Math.max(0, end - maxVisible + 1);

    // Prev
    ul.appendChild(pageItem('‹', current - 1, current === 0));

    if (start > 0) {
        ul.appendChild(pageItem('1', 0, false));
        if (start > 1) ul.appendChild(pageItem('…', -1, true));
    }

    for (let i = start; i <= end; i++) {
        ul.appendChild(pageItem(i + 1, i, false, i === current));
    }

    if (end < total - 1) {
        if (end < total - 2) ul.appendChild(pageItem('…', -1, true));
        ul.appendChild(pageItem(total, total - 1, false));
    }

    // Next
    ul.appendChild(pageItem('›', current + 1, current === total - 1));
}

function pageItem(label, page, disabled, active = false) {
    const li = document.createElement('li');
    li.className = `page-item${disabled ? ' disabled' : ''}${active ? ' active' : ''}`;
    const a = document.createElement('a');
    a.className = 'page-link';
    a.href = '#';
    a.textContent = label;
    if (!disabled && page >= 0) {
        a.addEventListener('click', (e) => {
            e.preventDefault();
            loadRecords(page, currentFilters);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }
    li.appendChild(a);
    return li;
}
