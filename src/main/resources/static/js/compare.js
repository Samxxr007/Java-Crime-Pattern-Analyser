/**
 * Area Comparison Engine
 * Renders radar charts, grouped bars, and side-by-side metric scorecards.
 */

let radarChart = null;
let barChart = null;

const CHENNAI_AREAS = [
    "Anna Nagar", "T Nagar", "Adyar", "Velachery", "Tambaram",
    "Guindy", "Egmore", "Mylapore", "Perambur", "Ambattur",
    "Porur", "Sholinganallur", "Thoraipakkam", "Chromepet", "Pallavaram",
    "Saidapet", "Nungambakkam", "Royapettah", "Triplicane", "Kodambakkam",
    "Ashok Nagar", "Vadapalani", "Mogappair", "Avadi", "Red Hills"
];

const ALL_CRIME_TYPES = [
    "Theft", "Vehicle Theft", "Cyber Crime", "Fraud", "Burglary",
    "Robbery", "Assault", "Chain Snatching", "Vandalism", "Harassment"
];

document.addEventListener('DOMContentLoaded', () => {
    populateAreaDropdowns();
    initEventListeners();
    loadComparison("Anna Nagar", "T Nagar");
});

function populateAreaDropdowns() {
    const sel1 = document.getElementById('selectArea1');
    const sel2 = document.getElementById('selectArea2');
    if (!sel1 || !sel2) return;

    sel1.innerHTML = '';
    sel2.innerHTML = '';

    CHENNAI_AREAS.forEach(area => {
        const opt1 = new Option(area, area, false, area === 'Anna Nagar');
        const opt2 = new Option(area, area, false, area === 'T Nagar');
        sel1.add(opt1);
        sel2.add(opt2);
    });
}

function initEventListeners() {
    const sel1 = document.getElementById('selectArea1');
    const sel2 = document.getElementById('selectArea2');

    const handleSelectChange = () => {
        const a1 = sel1.value;
        const a2 = sel2.value;
        if (a1 === a2) {
            alert('Please select two distinct areas to compare.');
            return;
        }
        loadComparison(a1, a2);
    };

    sel1.addEventListener('change', handleSelectChange);
    sel2.addEventListener('change', handleSelectChange);
}

function loadComparison(area1, area2) {
    fetch(`/api/analytics/compare?area1=${encodeURIComponent(area1)}&area2=${encodeURIComponent(area2)}`)
        .then(r => {
            if (!r.ok) throw new Error('Failed to load comparison data');
            return r.json();
        })
        .then(data => {
            renderScorecards(data);
            renderRadarChart(data);
            renderSeverityChart(data);
            renderComparisonTable(data);
        })
        .catch(err => {
            console.error(err);
            document.getElementById('compareSummaryText').textContent = 'Error loading comparison data: ' + err.message;
        });
}

function renderScorecards(d) {
    document.getElementById('titleArea1').textContent = d.area1;
    document.getElementById('titleArea2').textContent = d.area2;
    document.getElementById('tableSubtitle').textContent = `${d.area1} vs ${d.area2}`;
    document.getElementById('thArea1').textContent = d.area1;
    document.getElementById('thArea2').textContent = d.area2;

    document.getElementById('compareSummaryText').textContent = d.comparativeSummary;

    // Safety scores
    const s1 = d.safetyScoreArea1;
    const s2 = d.safetyScoreArea2;
    document.getElementById('safetyScore1').textContent = s1;
    document.getElementById('safetyScore2').textContent = s2;

    const circle1 = document.getElementById('scoreCircle1');
    const circle2 = document.getElementById('scoreCircle2');
    circle1.className = `score-circle ${s1 >= 75 ? 'safe' : s1 >= 50 ? 'moderate' : 'risk'} mb-2`;
    circle2.className = `score-circle ${s2 >= 75 ? 'safe' : s2 >= 50 ? 'moderate' : 'risk'} mb-2`;

    // Totals & CSI
    document.getElementById('totalIncidents1').textContent = d.totalIncidentsArea1;
    document.getElementById('totalIncidents2').textContent = d.totalIncidentsArea2;
    document.getElementById('csi1').textContent = d.crimeSeverityIndexArea1.toFixed(2);
    document.getElementById('csi2').textContent = d.crimeSeverityIndexArea2.toFixed(2);

    document.getElementById('dominantCrime1').textContent = d.dominantCrimeArea1;
    document.getElementById('dominantCrime2').textContent = d.dominantCrimeArea2;

    document.getElementById('peakTime1').textContent = d.peakTimeArea1;
    document.getElementById('peakTime2').textContent = d.peakTimeArea2;

    document.getElementById('hotspotScore1').textContent = d.hotspotScoreArea1.toFixed(3);
    document.getElementById('hotspotScore2').textContent = d.hotspotScoreArea2.toFixed(3);

    const b1 = document.getElementById('hotspotBadge1');
    b1.textContent = d.hotspotLevelArea1 + ' RISK';
    b1.className = `badge ${d.hotspotLevelArea1 === 'HIGH' ? 'bg-danger' : d.hotspotLevelArea1 === 'MODERATE' ? 'bg-warning text-dark' : 'bg-success'}`;

    const b2 = document.getElementById('hotspotBadge2');
    b2.textContent = d.hotspotLevelArea2 + ' RISK';
    b2.className = `badge ${d.hotspotLevelArea2 === 'HIGH' ? 'bg-danger' : d.hotspotLevelArea2 === 'MODERATE' ? 'bg-warning text-dark' : 'bg-success'}`;
}

function renderRadarChart(d) {
    const ctx = document.getElementById('radarCompareChart');
    if (!ctx) return;

    const data1 = ALL_CRIME_TYPES.map(t => d.crimeDistributionArea1[t] || 0);
    const data2 = ALL_CRIME_TYPES.map(t => d.crimeDistributionArea2[t] || 0);

    if (radarChart) radarChart.destroy();

    radarChart = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ALL_CRIME_TYPES,
            datasets: [
                {
                    label: d.area1,
                    data: data1,
                    backgroundColor: 'rgba(59, 130, 246, 0.25)',
                    borderColor: '#3b82f6',
                    pointBackgroundColor: '#3b82f6',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: '#3b82f6',
                    borderWidth: 2
                },
                {
                    label: d.area2,
                    data: data2,
                    backgroundColor: 'rgba(239, 68, 68, 0.25)',
                    borderColor: '#ef4444',
                    pointBackgroundColor: '#ef4444',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: '#ef4444',
                    borderWidth: 2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            elements: { line: { tension: 0.2 } },
            scales: {
                r: {
                    angleLines: { color: 'rgba(150, 150, 150, 0.2)' },
                    grid: { color: 'rgba(150, 150, 150, 0.2)' },
                    pointLabels: { font: { size: 11 } }
                }
            },
            plugins: {
                legend: { position: 'top' }
            }
        }
    });
}

function renderSeverityChart(d) {
    const ctx = document.getElementById('severityCompareChart');
    if (!ctx) return;

    const severities = ['HIGH', 'MEDIUM', 'LOW'];
    const s1 = severities.map(s => d.severityDistributionArea1[s] || 0);
    const s2 = severities.map(s => d.severityDistributionArea2[s] || 0);

    if (barChart) barChart.destroy();

    barChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['High Severity', 'Medium Severity', 'Low Severity'],
            datasets: [
                {
                    label: d.area1,
                    data: s1,
                    backgroundColor: '#3b82f6',
                    borderRadius: 4
                },
                {
                    label: d.area2,
                    data: s2,
                    backgroundColor: '#ef4444',
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(150, 150, 150, 0.15)' } },
                x: { grid: { display: false } }
            },
            plugins: {
                legend: { position: 'top' }
            }
        }
    });
}

function renderComparisonTable(d) {
    const tbody = document.getElementById('comparisonTableBody');
    if (!tbody) return;

    let html = '';
    ALL_CRIME_TYPES.forEach(type => {
        const c1 = d.crimeDistributionArea1[type] || 0;
        const c2 = d.crimeDistributionArea2[type] || 0;
        const diff = c1 - c2;

        let diffBadge = '';
        if (diff > 0) {
            diffBadge = `<span class="badge bg-primary-subtle text-primary">+${diff} in ${d.area1}</span>`;
        } else if (diff < 0) {
            diffBadge = `<span class="badge bg-danger-subtle text-danger">+${Math.abs(diff)} in ${d.area2}</span>`;
        } else {
            diffBadge = `<span class="badge bg-secondary-subtle text-secondary">Equal</span>`;
        }

        let assess = 'Comparable Risk';
        if (Math.abs(diff) >= 5) {
            assess = diff > 0 ? `Higher vulnerability in ${d.area1}` : `Higher vulnerability in ${d.area2}`;
        }

        html += `
            <tr>
                <td class="fw-semibold text-body">${type}</td>
                <td class="text-center font-monospace">${c1}</td>
                <td class="text-center font-monospace">${c2}</td>
                <td class="text-center">${diffBadge}</td>
                <td class="text-center small text-secondary">${assess}</td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}
