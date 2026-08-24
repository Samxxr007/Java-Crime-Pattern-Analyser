/**
 * dashboard.js — Dashboard page logic
 * Loads stats and renders all Chart.js charts from the analytics API.
 */

'use strict';

// Chart instances (held globally so they can be destroyed on refresh)
let areaChart, typeChart, monthChart, hourChart, severityChart, yearChart, dayChart;

// -------------------------------------------------------
// Entry point
// -------------------------------------------------------
document.addEventListener('DOMContentLoaded', async () => {
    await loadDashboard();
});

async function loadDashboard() {
    try {
        const stats = await apiFetch('/api/analytics/summary');
        renderStatCards(stats);
        renderAreaChart(stats.crimesByArea);
        renderTypeChart(stats.crimesByType);
        renderMonthChart(stats.crimesByMonth);
        renderHourChart(stats.crimesByHour);
        renderSeverityChart(stats.severityDistribution);
        renderYearChart(stats.crimesByYear);
        renderDayChart(stats.crimesByDayOfWeek);
    } catch (err) {
        console.error('Dashboard load error:', err);
        showToast('Failed to load dashboard data. Is the server running?', 'error');
    }

    try {
        const hotspots = await apiFetch('/api/analytics/hotspots');
        renderHotspotTable(hotspots);
    } catch (err) {
        console.error('Hotspot load error:', err);
    }
}

// -------------------------------------------------------
// Stat cards
// -------------------------------------------------------
function renderStatCards(stats) {
    const el = id => document.getElementById(id);

    el('totalRecords').textContent = fmt(stats.totalRecords);
    el('highSeverity').textContent = fmt(stats.highSeverityCount);
    el('topCrime').textContent     = stats.mostFrequentCrimeType || '—';
    el('topArea').textContent      = stats.mostActiveArea || '—';
    el('totalAreas').textContent   = fmt(stats.totalAreas);
    el('closedCases').textContent  = fmt(stats.statusDistribution?.CLOSED || 0);
}

// -------------------------------------------------------
// Charts
// -------------------------------------------------------

function renderAreaChart(data) {
    if (!data) return;
    // Sort descending, take top 15
    const sorted = Object.entries(data)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 15);
    const labels = sorted.map(e => e[0]);
    const values = sorted.map(e => e[1]);

    destroyChart(areaChart);
    areaChart = new Chart(document.getElementById('areaChart'), {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: 'Incidents',
                data: values,
                backgroundColor: CHART_COLORS.slice(0, labels.length),
                borderRadius: 6,
                borderSkipped: false,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false }, ticks: { maxRotation: 40, font: { size: 11 } } },
                y: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: { stepSize: 20 } }
            }
        }
    });
}

function renderTypeChart(data) {
    if (!data) return;
    const entries = Object.entries(data).sort((a, b) => b[1] - a[1]);
    const labels  = entries.map(e => e[0]);
    const values  = entries.map(e => e[1]);

    destroyChart(typeChart);
    typeChart = new Chart(document.getElementById('typeChart'), {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: CHART_COLORS,
                borderWidth: 2,
                borderColor: '#fff',
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '55%',
            plugins: {
                legend: { position: 'bottom', labels: { font: { size: 11 }, padding: 10 } }
            }
        }
    });
}

function renderMonthChart(data) {
    if (!data) return;
    const months = Array.from({ length: 12 }, (_, i) => i + 1);
    const labels = months.map(m => monthName(m));
    const values = months.map(m => data[m] || 0);

    destroyChart(monthChart);
    monthChart = new Chart(document.getElementById('monthChart'), {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: 'Incidents',
                data: values,
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59,130,246,0.12)',
                borderWidth: 2.5,
                pointRadius: 4,
                pointBackgroundColor: '#3b82f6',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false } },
                y: { beginAtZero: true, grid: { color: '#f1f5f9' } }
            }
        }
    });
}

function renderHourChart(data) {
    if (!data) return;
    const hours  = Array.from({ length: 24 }, (_, i) => i);
    const labels = hours.map(h => hourLabel(h));
    const values = hours.map(h => data[h] || 0);

    destroyChart(hourChart);
    hourChart = new Chart(document.getElementById('hourChart'), {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: 'Incidents',
                data: values,
                backgroundColor: values.map(v => v === Math.max(...values) ? '#ef4444' : '#3b82f6'),
                borderRadius: 4,
                borderSkipped: false,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false }, ticks: { maxTicksLimit: 12, font: { size: 10 } } },
                y: { beginAtZero: true, grid: { color: '#f1f5f9' } }
            }
        }
    });
}

function renderSeverityChart(data) {
    if (!data) return;
    const labels = ['HIGH', 'MEDIUM', 'LOW'];
    const values = labels.map(l => data[l] || 0);
    const colors = ['#ef4444', '#f97316', '#22c55e'];

    destroyChart(severityChart);
    severityChart = new Chart(document.getElementById('severityChart'), {
        type: 'pie',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: '#fff',
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom', labels: { font: { size: 12 }, padding: 14 } }
            }
        }
    });
}

function renderYearChart(data) {
    if (!data) return;
    const years  = Object.keys(data).sort();
    const values = years.map(y => data[y] || 0);

    destroyChart(yearChart);
    yearChart = new Chart(document.getElementById('yearChart'), {
        type: 'line',
        data: {
            labels: years,
            datasets: [{
                label: 'Incidents',
                data: values,
                borderColor: '#8b5cf6',
                backgroundColor: 'rgba(139,92,246,0.12)',
                borderWidth: 2.5,
                pointRadius: 5,
                pointBackgroundColor: '#8b5cf6',
                fill: true,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false } },
                y: { beginAtZero: true, grid: { color: '#f1f5f9' } }
            }
        }
    });
}

function renderDayChart(data) {
    if (!data) return;
    const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    const labels   = dayOrder.filter(d => data[d] !== undefined);
    const values   = labels.map(d => data[d] || 0);

    destroyChart(dayChart);
    dayChart = new Chart(document.getElementById('dayChart'), {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: 'Incidents',
                data: values,
                backgroundColor: '#14b8a6',
                borderRadius: 6,
                borderSkipped: false,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false } },
                y: { beginAtZero: true, grid: { color: '#f1f5f9' } }
            }
        }
    });
}

// -------------------------------------------------------
// Hotspot table
// -------------------------------------------------------
function renderHotspotTable(hotspots) {
    const tbody = document.getElementById('hotspotBody');
    if (!hotspots || hotspots.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No data available.</td></tr>';
        return;
    }

    tbody.innerHTML = hotspots.map((h, i) => `
        <tr>
            <td class="text-muted">${i + 1}</td>
            <td class="fw-semibold">${h.area}</td>
            <td><span class="chart-badge">${h.zone}</span></td>
            <td>${fmt(h.totalCrimes)}</td>
            <td>${scoreBar(h.hotspotScore, h.hotspotLevel)}</td>
            <td>${hotspotBadge(h.hotspotLevel)}</td>
            <td>${h.dominantCrimeType || '—'}</td>
        </tr>
    `).join('');
}
