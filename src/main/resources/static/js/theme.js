/**
 * Command Center Theme & Global Utilities
 * Manages Dark/Light mode persistence and Live Dispatch Ticker.
 */
document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Dark/Light mode from localStorage
    const savedTheme = localStorage.getItem('crime_theme') || 'light';
    applyTheme(savedTheme);

    const themeToggleBtn = document.getElementById('themeToggleBtn');
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
            applyTheme(nextTheme);
            localStorage.setItem('crime_theme', nextTheme);
        });
    }

    // 2. Initialize Live Dispatch Ticker
    initLiveDispatchTicker();
});

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    const themeIcon = document.getElementById('themeIcon');
    if (themeIcon) {
        if (theme === 'dark') {
            themeIcon.className = 'bi bi-sun-fill text-warning';
        } else {
            themeIcon.className = 'bi bi-moon-stars-fill text-secondary';
        }
    }
}

function initLiveDispatchTicker() {
    const tickerEl = document.getElementById('liveTickerText');
    if (!tickerEl) return;

    fetch('/api/analytics/live-feed')
        .then(r => r.json())
        .then(data => {
            if (!Array.isArray(data) || data.length === 0) return;
            let index = 0;
            const updateTicker = () => {
                const item = data[index % data.length];
                tickerEl.innerHTML = `<span class="fw-semibold text-primary">[${item.crimeTime || 'Recent'}]</span> ` +
                    `<span class="badge bg-secondary-subtle text-body me-1">${item.area}</span> ` +
                    `${item.crimeType} reported (${item.severity} SEVERITY) — Status: <span class="fw-medium text-success">${item.caseStatus}</span>`;
                index++;
            };
            updateTicker();
            setInterval(updateTicker, 4000);
        })
        .catch(() => {
            tickerEl.textContent = 'Command Center Live Monitoring Active • Simulated Feeds Synchronized';
        });
}
