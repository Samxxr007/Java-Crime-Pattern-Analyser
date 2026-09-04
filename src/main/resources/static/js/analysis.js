/**
 * analysis.js — Pattern Analysis page
 * Fetches per-area analysis from API and renders 5 charts.
 */

'use strict';

let aMonthChart, aHourChart, aTypeChart, aSeverityChart, aDayChart;
let cachedAnalysisData = null;

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

document.addEventListener('DOMContentLoaded', async () => {
    const areaSelect = document.getElementById('analysisArea');
    await loadAreaDropdown(areaSelect, false);

    document.getElementById('analyseBtn').addEventListener('click', () => {
        const area = areaSelect.value;
        if (!area) {
            showToast('Please select an area first.', 'warning');
            return;
        }
        runAnalysis(area);
    });

    areaSelect.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') document.getElementById('analyseBtn').click();
    });

    window.addEventListener('themeChanged', () => {
        if (cachedAnalysisData) {
            renderAreaCharts(cachedAnalysisData);
        }
    });
});

async function runAnalysis(area) {
    const resultsEl = document.getElementById('analysisResults');
    const emptyEl   = document.getElementById('analysisEmpty');
    const btn       = document.getElementById('analyseBtn');

    btn.disabled    = true;
    btn.innerHTML   = '<span class="spinner-border spinner-border-sm me-1"></span>Analysing…';
    resultsEl.style.display = 'none';
    emptyEl.style.display   = 'none';

    try {
        const data = await apiFetch(`/api/analytics/area/${encodeURIComponent(area)}`);

        if (!data) {
            emptyEl.innerHTML = `<i class="bi bi-search display-3 text-muted opacity-50"></i>
                <p class="text-muted mt-3">No data found for <strong>${area}</strong>.</p>`;
            emptyEl.style.display = 'block';
            return;
        }

        cachedAnalysisData = data;
        populateSummaryCards(data);
        renderAreaCharts(data);
        resultsEl.style.display = 'block';
        resultsEl.scrollIntoView({ behavior: 'smooth', block: 'start' });

    } catch (err) {
        console.error('Analysis error:', err);
        emptyEl.innerHTML = `<i class="bi bi-exclamation-circle display-3 text-danger opacity-60"></i>
            <p class="text-muted mt-3">Error loading analysis for <strong>${area}</strong>. Please try again.</p>`;
        emptyEl.style.display = 'block';
    } finally {
        btn.disabled  = false;
        btn.innerHTML = '<i class="bi bi-bar-chart-fill me-1"></i>Analyse';
    }
}

function populateSummaryCards(d) {
    document.getElementById('aTotal').textContent   = fmt(d.totalRecords);
    document.getElementById('aTopCrime').textContent  = d.mostCommonCrimeType || '—';
    document.getElementById('aPeakTime').textContent  = d.peakHourRange || '—';
    document.getElementById('aSeverity').textContent  = d.dominantSeverity || '—';
    document.getElementById('aPeakDay').textContent   = (d.mostActiveDayOfWeek || '—').substring(0, 3);

    const hotspotEl = document.getElementById('aHotspot');
    hotspotEl.textContent = d.hotspotLevel || '—';

    // Color the hotspot card
    const card = document.getElementById('hotspotCard');
    card.className = 'stat-card ' + {
        HIGH:     'stat-card--red',
        MODERATE: 'stat-card--orange',
        LOW:      'stat-card--green'
    }[d.hotspotLevel] || 'stat-card--teal';
}

function renderAreaCharts(d) {
    const { textColor, tickColor, gridColor, borderColor } = resolveChartColors();

    // Monthly trend
    const months = Array.from({ length: 12 }, (_, i) => i + 1);
    destroyChart(aMonthChart);
    aMonthChart = new Chart(document.getElementById('aMonthChart'), {
        type: 'line',
        data: {
            labels: months.map(m => monthName(m)),
            datasets: [{
                label: 'Incidents',
                data: months.map(m => d.monthlyTrend?.[m] || 0),
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59,130,246,0.15)',
                borderWidth: 2.5,
                pointRadius: 4,
                fill: true,
                tension: 0.4
            }]
        },
        options: chartOpts()
    });

    // Hourly distribution
    const hours = Array.from({ length: 24 }, (_, i) => i);
    destroyChart(aHourChart);
    aHourChart = new Chart(document.getElementById('aHourChart'), {
        type: 'bar',
        data: {
            labels: hours.map(h => hourLabel(h)),
            datasets: [{
                label: 'Incidents',
                data: hours.map(h => d.hourlyTrend?.[h] || 0),
                backgroundColor: hours.map(h => h === d.peakHour ? '#ef4444' : '#3b82f6'),
                borderRadius: 4,
                borderSkipped: false,
            }]
        },
        options: { 
            ...chartOpts(), 
            scales: {
                x: { 
                    grid: { display: false }, 
                    ticks: { color: tickColor, maxTicksLimit: 8, font: { weight: '500' } } 
                },
                y: { 
                    beginAtZero: true, 
                    grid: { color: gridColor },
                    ticks: { color: tickColor, font: { weight: '500' } }
                }
            }
        }
    });

    // Crime type breakdown
    const typeEntries = Object.entries(d.crimeTypeDistribution || {}).sort((a,b) => b[1]-a[1]);
    destroyChart(aTypeChart);
    aTypeChart = new Chart(document.getElementById('aTypeChart'), {
        type: 'doughnut',
        data: {
            labels: typeEntries.map(e => e[0]),
            datasets: [{
                data: typeEntries.map(e => e[1]),
                backgroundColor: CHART_COLORS,
                borderWidth: 2,
                borderColor: borderColor
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '50%',
            plugins: { 
                legend: { 
                    position: 'bottom', 
                    labels: { color: textColor, font: { size: 11, weight: '500' }, padding: 8 } 
                } 
            }
        }
    });

    // Severity
    const sevOrder = ['HIGH', 'MEDIUM', 'LOW'];
    destroyChart(aSeverityChart);
    aSeverityChart = new Chart(document.getElementById('aSeverityChart'), {
        type: 'bar',
        data: {
            labels: sevOrder,
            datasets: [{
                label: 'Incidents',
                data: sevOrder.map(s => d.severityDistribution?.[s] || 0),
                backgroundColor: ['#ef4444', '#f97316', '#22c55e'],
                borderRadius: 6,
                borderSkipped: false,
            }]
        },
        options: chartOpts()
    });

    // Day of week
    const dayOrder = ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'];
    destroyChart(aDayChart);
    aDayChart = new Chart(document.getElementById('aDayChart'), {
        type: 'bar',
        data: {
            labels: dayOrder.map(d => d.substring(0,3)),
            datasets: [{
                label: 'Incidents',
                data: dayOrder.map(day => d.dayOfWeekDistribution?.[day] || 0),
                backgroundColor: '#14b8a6',
                borderRadius: 6,
                borderSkipped: false,
            }]
        },
        options: chartOpts()
    });
}

function chartOpts() {
    const { tickColor, gridColor } = resolveChartColors();
    return {
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
    };
}
