/**
 * dashboard.js — Dashboard page logic
 * Loads stats and renders all Chart.js charts from the analytics API.
 */

'use strict';

// Chart instances (held globally so they can be destroyed on refresh)
let areaChart, typeChart, monthChart, hourChart, severityChart, yearChart, dayChart;

// Cached stats for instantaneous theme-switch re-rendering
let cachedStats = null;

const FALLBACK_CHART_COLORS = [
    '#3b82f6', '#ef4444', '#f97316', '#8b5cf6', '#14b8a6',
    '#22c55e', '#eab308', '#06b6d4', '#f43f5e', '#64748b',
    '#0ea5e9', '#a855f7', '#84cc16', '#fb923c', '#6366f1'
];

function resolveChartColors() {
    if (typeof getChartThemeColors === 'function') {
        try { return getChartThemeColors(); } catch(e) {}
    }
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    return {
        textColor: isDark ? '#ffffff' : '#0f172a',
        tickColor: isDark ? '#f1f5f9' : '#334155',
        gridColor: isDark ? 'rgba(255, 255, 255, 0.16)' : 'rgba(0, 0, 0, 0.08)',
        borderColor: isDark ? '#111827' : '#ffffff'
    };
}

function getPalette() {
    return (typeof CHART_COLORS !== 'undefined' && Array.isArray(CHART_COLORS))
        ? CHART_COLORS
        : FALLBACK_CHART_COLORS;
}

// -------------------------------------------------------
// Entry point
// -------------------------------------------------------
document.addEventListener('DOMContentLoaded', async () => {
    await loadDashboard();

    // Listen for dark/light mode toggle and re-render charts immediately
    window.addEventListener('themeChanged', () => {
        if (cachedStats) {
            renderAllCharts(cachedStats);
        }
    });
});

async function loadDashboard() {
    try {
        const stats = await apiFetch('/api/analytics/summary');
        cachedStats = stats;
        renderStatCards(stats);
        try {
            renderAllCharts(stats);
        } catch (chartErr) {
            console.error('Charts rendering error:', chartErr);
        }
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

function renderAllCharts(stats) {
    if (!stats || typeof Chart === 'undefined') {
        if (typeof Chart === 'undefined') {
            console.warn('Chart.js library not yet ready.');
        }
        return;
    }
    try { renderAreaChart(stats.crimesByArea); } catch(e) { console.error('renderAreaChart error:', e); }
    try { renderTypeChart(stats.crimesByType); } catch(e) { console.error('renderTypeChart error:', e); }
    try { renderMonthChart(stats.crimesByMonth); } catch(e) { console.error('renderMonthChart error:', e); }
    try { renderHourChart(stats.crimesByHour); } catch(e) { console.error('renderHourChart error:', e); }
    try { renderSeverityChart(stats.severityDistribution); } catch(e) { console.error('renderSeverityChart error:', e); }
    try { renderYearChart(stats.crimesByYear); } catch(e) { console.error('renderYearChart error:', e); }
    try { renderDayChart(stats.crimesByDayOfWeek); } catch(e) { console.error('renderDayChart error:', e); }
}

// -------------------------------------------------------
// Stat cards
// -------------------------------------------------------
function renderStatCards(stats) {
    const el = id => document.getElementById(id);

    animateValue('totalRecords', 0, stats.totalRecords || 0, 800);
    animateValue('highSeverity', 0, stats.highSeverityCount || 0, 800);
    animateValue('totalAreas', 0, stats.totalAreas || 0, 600);
    animateValue('closedCases', 0, stats.statusDistribution?.CLOSED || 0, 800);

    const safetyEl = el('citySafetyIndex');
    if (safetyEl) {
        animateValue('citySafetyIndex', 0, stats.citySafetyIndex || 74, 800);
    }

    const csiEl = el('crimeSeverityIndex');
    if (csiEl) {
        csiEl.textContent = (stats.crimeSeverityIndex || 1.79).toFixed(2);
    }

    el('topCrime').textContent = stats.mostFrequentCrimeType || '—';
    el('topArea').textContent  = stats.mostActiveArea || '—';
}

function animateValue(id, start, end, duration) {
    const obj = document.getElementById(id);
    if (!obj) return;
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        obj.textContent = Math.floor(progress * (end - start) + start).toLocaleString();
        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    };
    window.requestAnimationFrame(step);
}

// -------------------------------------------------------
// Charts with dynamic contrast synchronization
// -------------------------------------------------------

function renderAreaChart(data) {
    if (!data) return;
    const canvas = document.getElementById('areaChart');
    if (!canvas) return;

    const sorted = Object.entries(data)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 15);
    const labels = sorted.map(e => e[0]);
    const values = sorted.map(e => e[1]);
    const { tickColor, gridColor } = resolveChartColors();
    const palette = getPalette();

    destroyChart(areaChart);
    areaChart = new Chart(canvas, {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: 'Incidents',
                data: values,
                backgroundColor: palette.slice(0, labels.length),
                borderRadius: 6,
                borderSkipped: false,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { 
                    grid: { display: false }, 
                    ticks: { color: tickColor, maxRotation: 40, font: { size: 11, weight: '500' } } 
                },
                y: { 
                    beginAtZero: true, 
                    grid: { color: gridColor }, 
                    ticks: { color: tickColor, stepSize: 20, font: { weight: '500' } } 
                }
            }
        }
    });
}

function renderTypeChart(data) {
    if (!data) return;
    const canvas = document.getElementById('typeChart');
    if (!canvas) return;

    const entries = Object.entries(data).sort((a, b) => b[1] - a[1]);
    const labels  = entries.map(e => e[0]);
    const values  = entries.map(e => e[1]);
    const { textColor, borderColor } = resolveChartColors();
    const palette = getPalette();

    destroyChart(typeChart);
    typeChart = new Chart(canvas, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: palette,
                borderWidth: 2,
                borderColor: borderColor,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '55%',
            plugins: {
                legend: { 
                    position: 'bottom', 
                    labels: { color: textColor, font: { size: 11, weight: '500' }, padding: 10 } 
                }
            }
        }
    });
}

function renderMonthChart(data) {
    if (!data) return;
    const canvas = document.getElementById('monthChart');
    if (!canvas) return;

    const months = Array.from({ length: 12 }, (_, i) => i + 1);
    const labels = months.map(m => (typeof monthName === 'function' ? monthName(m) : 'M' + m));
    const values = months.map(m => data[m] || 0);
    const { tickColor, gridColor } = resolveChartColors();

    destroyChart(monthChart);
    monthChart = new Chart(canvas, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: 'Incidents',
                data: values,
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59,130,246,0.15)',
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
                x: { 
                    grid: { display: false },
                    ticks: { color: tickColor, font: { weight: '500' } }
                },
                y: { 
                    beginAtZero: true, 
                    grid: { color: gridColor },
                    ticks: { color: tickColor, font: { weight: '500' } }
                }
            }
        }
    });
}

function renderHourChart(data) {
    if (!data) return;
    const canvas = document.getElementById('hourChart');
    if (!canvas) return;

    const hours  = Array.from({ length: 24 }, (_, i) => i);
    const labels = hours.map(h => (typeof hourLabel === 'function' ? hourLabel(h) : h + ':00'));
    const values = hours.map(h => data[h] || 0);
    const { tickColor, gridColor } = resolveChartColors();

    destroyChart(hourChart);
    hourChart = new Chart(canvas, {
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
                x: { 
                    grid: { display: false }, 
                    ticks: { color: tickColor, maxTicksLimit: 12, font: { size: 10, weight: '500' } } 
                },
                y: { 
                    beginAtZero: true, 
                    grid: { color: gridColor },
                    ticks: { color: tickColor, font: { weight: '500' } }
                }
            }
        }
    });
}

function renderSeverityChart(data) {
    if (!data) return;
    const canvas = document.getElementById('severityChart');
    if (!canvas) return;

    const labels = ['HIGH', 'MEDIUM', 'LOW'];
    const values = labels.map(l => data[l] || 0);
    const colors = ['#ef4444', '#f97316', '#22c55e'];
    const { textColor, borderColor } = resolveChartColors();

    destroyChart(severityChart);
    severityChart = new Chart(canvas, {
        type: 'pie',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: borderColor,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { 
                    position: 'bottom', 
                    labels: { color: textColor, font: { size: 12, weight: '600' }, padding: 14 } 
                }
            }
        }
    });
}

function renderYearChart(data) {
    if (!data) return;
    const canvas = document.getElementById('yearChart');
    if (!canvas) return;

    const years  = Object.keys(data).sort();
    const values = years.map(y => data[y] || 0);
    const { tickColor, gridColor } = resolveChartColors();

    destroyChart(yearChart);
    yearChart = new Chart(canvas, {
        type: 'line',
        data: {
            labels: years,
            datasets: [{
                label: 'Incidents',
                data: values,
                borderColor: '#8b5cf6',
                backgroundColor: 'rgba(139,92,246,0.15)',
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
                x: { 
                    grid: { display: false },
                    ticks: { color: tickColor, font: { weight: '500' } }
                },
                y: { 
                    beginAtZero: true, 
                    grid: { color: gridColor },
                    ticks: { color: tickColor, font: { weight: '500' } }
                }
            }
        }
    });
}

function renderDayChart(data) {
    if (!data) return;
    const canvas = document.getElementById('dayChart');
    if (!canvas) return;

    const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    const labels   = dayOrder.filter(d => data[d] !== undefined);
    const values   = labels.map(d => data[d] || 0);
    const { tickColor, gridColor } = resolveChartColors();

    destroyChart(dayChart);
    dayChart = new Chart(canvas, {
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
                x: { 
                    grid: { display: false },
                    ticks: { color: tickColor, font: { weight: '500' } }
                },
                y: { 
                    beginAtZero: true, 
                    grid: { color: gridColor },
                    ticks: { color: tickColor, font: { weight: '500' } }
                }
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
