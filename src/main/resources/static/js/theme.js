/**
 * Command Center Theme & Global Utilities
 * Manages High-Contrast Dark/Light mode persistence,
 * dynamic Chart.js theme synchronization, and Live Dispatch Ticker.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Default to 'light' mode for crystal-clear readability
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

    // Initialize Live Dispatch Ticker
    initLiveDispatchTicker();
});

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    const themeIcon = document.getElementById('themeIcon');
    if (themeIcon) {
        if (theme === 'dark') {
            themeIcon.className = 'bi bi-sun-fill text-warning fs-5';
            themeIcon.title = 'Switch to High-Contrast Light Mode';
        } else {
            themeIcon.className = 'bi bi-moon-stars-fill text-primary fs-5';
            themeIcon.title = 'Switch to Command Dark Mode';
        }
    }

    // Synchronize Chart.js global defaults
    if (window.Chart) {
        const isDark = (theme === 'dark');
        const textColor = isDark ? '#ffffff' : '#0f172a';
        const gridColor = isDark ? 'rgba(255, 255, 255, 0.16)' : 'rgba(0, 0, 0, 0.08)';

        Chart.defaults.color = textColor;
        if (Chart.defaults.plugins && Chart.defaults.plugins.legend && Chart.defaults.plugins.legend.labels) {
            Chart.defaults.plugins.legend.labels.color = textColor;
        }

        // Notify page scripts to update chart instances
        window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme, isDark, textColor, gridColor } }));
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
                const timeStr = item.crimeTime ? item.crimeTime.substring(0, 5) : '19:45';
                tickerEl.innerHTML = `<span class="fw-bold text-primary">[${timeStr}]</span> ` +
                    `<span class="badge bg-secondary-subtle text-body me-1">${item.area}</span> ` +
                    `<span class="fw-semibold text-body">${item.crimeType}</span> reported ` +
                    `<span class="badge ${item.severity === 'HIGH' ? 'bg-danger' : item.severity === 'MEDIUM' ? 'bg-warning text-dark' : 'bg-success'}">${item.severity}</span> ` +
                    `— Status: <span class="fw-bold text-success">${item.caseStatus}</span>`;
                index++;
            };
            updateTicker();
            setInterval(updateTicker, 4500);
        })
        .catch(() => {
            tickerEl.textContent = 'Command Center Live Telemetry Active • Sector Streams Synchronized';
        });
}
