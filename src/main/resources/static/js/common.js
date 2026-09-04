/**
 * common.js — Shared utilities for all pages
 * Chennai Crime Pattern Analyzer
 */

'use strict';

// -------------------------------------------------------
// Sidebar toggle
// -------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    const sidebar  = document.getElementById('sidebar');
    const toggle   = document.getElementById('sidebarToggle');
    const isMobile = () => window.innerWidth < 768;

    if (toggle && sidebar) {
        toggle.addEventListener('click', () => {
            if (isMobile()) {
                sidebar.classList.toggle('mobile-open');
            } else {
                sidebar.classList.toggle('collapsed');
            }
        });

        // Close sidebar on mobile when clicking outside
        document.addEventListener('click', (e) => {
            if (isMobile() && sidebar.classList.contains('mobile-open')
                && !sidebar.contains(e.target) && e.target !== toggle) {
                sidebar.classList.remove('mobile-open');
            }
        });
    }
});

// -------------------------------------------------------
// API fetch helper
// -------------------------------------------------------

/**
 * Fetches a JSON resource from the API with error handling.
 * @param {string} url
 * @returns {Promise<any>}
 */
async function apiFetch(url) {
    const response = await fetch(url);
    if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.message || `HTTP ${response.status}`);
    }
    return response.json();
}

// -------------------------------------------------------
// Formatting utilities
// -------------------------------------------------------

/** Formats a number with locale-aware thousand separators. */
function fmt(n) {
    if (n === null || n === undefined) return '—';
    return Number(n).toLocaleString('en-IN');
}

/** Formats a date string (ISO) as dd/MM/yyyy. */
function fmtDate(isoStr) {
    if (!isoStr) return '—';
    const [y, m, d] = isoStr.split('-');
    return `${d}/${m}/${y}`;
}

/** Returns a severity badge HTML element. */
function severityBadge(sev) {
    const s = (sev || '').toUpperCase();
    return `<span class="sev-badge sev-${s}">${s || '—'}</span>`;
}

/** Returns a case status badge HTML element. */
function statusBadge(status) {
    const s = (status || '').toUpperCase();
    const label = s.replace(/_/g, ' ');
    return `<span class="status-badge status-${s}">${label || '—'}</span>`;
}

/** Returns a hotspot level badge HTML element. */
function hotspotBadge(level) {
    const l = (level || '').toUpperCase();
    return `<span class="hotspot-${l}">${l || '—'}</span>`;
}

/** Returns a score bar element for hotspot visual. */
function scoreBar(score, level) {
    const pct = Math.round(score * 100);
    return `<div class="d-flex align-items-center gap-2">
        <div class="score-bar-wrap flex-grow-1">
            <div class="score-bar ${level}" style="width:${pct}%"></div>
        </div>
        <small class="text-muted fw-mono">${pct}%</small>
    </div>`;
}

/** Month names array (1-indexed). */
const MONTH_NAMES = [
    '', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
];

/** Converts month number to abbreviated name. */
function monthName(n) {
    return MONTH_NAMES[parseInt(n)] || n;
}

/** Formats an hour (0-23) as HH:00. */
function hourLabel(h) {
    return String(h).padStart(2, '0') + ':00';
}

// -------------------------------------------------------
// Chart.js High-Contrast Dynamic Palette
// -------------------------------------------------------
const CHART_COLORS = [
    '#3b82f6', '#ef4444', '#f97316', '#8b5cf6', '#14b8a6',
    '#22c55e', '#eab308', '#06b6d4', '#f43f5e', '#64748b',
    '#0ea5e9', '#a855f7', '#84cc16', '#fb923c', '#6366f1'
];
window.CHART_COLORS = CHART_COLORS;

function getChartThemeColors() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    return {
        textColor: isDark ? '#ffffff' : '#0f172a',
        tickColor: isDark ? '#f1f5f9' : '#334155',
        gridColor: isDark ? 'rgba(255, 255, 255, 0.16)' : 'rgba(0, 0, 0, 0.08)',
        borderColor: isDark ? '#111827' : '#ffffff'
    };
}
window.getChartThemeColors = getChartThemeColors;

/** Applies high-contrast styling to Chart.js if loaded. */
if (typeof Chart !== 'undefined' && Chart.defaults) {
    Chart.defaults.font = Chart.defaults.font || {};
    Chart.defaults.font.family = "'Segoe UI', system-ui, sans-serif";
    Chart.defaults.font.size   = 12;
    Chart.defaults.color       = document.documentElement.getAttribute('data-theme') === 'dark' ? '#ffffff' : '#1e293b';
    if (Chart.defaults.plugins && Chart.defaults.plugins.legend && Chart.defaults.plugins.legend.labels) {
        Chart.defaults.plugins.legend.labels.usePointStyle = true;
        Chart.defaults.plugins.legend.labels.color = document.documentElement.getAttribute('data-theme') === 'dark' ? '#ffffff' : '#1e293b';
    }
}

/** Destroys a chart instance safely before re-creating. */
function destroyChart(chartVar) {
    if (chartVar && typeof chartVar.destroy === 'function') {
        try {
            chartVar.destroy();
        } catch (e) {
            console.warn('Could not destroy chart instance:', e);
        }
    }
}
window.destroyChart = destroyChart;

// -------------------------------------------------------
// Toast / notification
// -------------------------------------------------------
function showToast(message, type = 'info') {
    const colors = { info: '#3b82f6', success: '#22c55e', error: '#ef4444', warning: '#f97316' };
    const div = document.createElement('div');
    div.style.cssText = `
        position:fixed; bottom:24px; right:24px; z-index:9999;
        background:${colors[type] || colors.info}; color:#fff;
        padding:12px 20px; border-radius:8px; font-size:13px;
        box-shadow:0 4px 16px rgba(0,0,0,.2); max-width:320px;
        animation: fadeIn 0.3s ease;
    `;
    div.textContent = message;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 4000);
}

// -------------------------------------------------------
// Load dropdowns
// -------------------------------------------------------
async function loadAreaDropdown(selectEl, includeAll = true) {
    try {
        const areas = await apiFetch('/api/crimes/distinct/areas');
        if (includeAll) {
            selectEl.innerHTML = '<option value="">All Areas</option>';
        } else {
            selectEl.innerHTML = '<option value="">— Select an area —</option>';
        }
        areas.forEach(a => {
            const opt = document.createElement('option');
            opt.value = a;
            opt.textContent = a;
            selectEl.appendChild(opt);
        });
    } catch (err) {
        console.warn('Could not load areas:', err);
    }
}

async function loadTypeDropdown(selectEl, includeAll = true) {
    try {
        const types = await apiFetch('/api/crimes/distinct/types');
        if (includeAll) {
            selectEl.innerHTML = '<option value="">All Types</option>';
        } else {
            selectEl.innerHTML = '<option value="">— Select type —</option>';
        }
        types.forEach(t => {
            const opt = document.createElement('option');
            opt.value = t;
            opt.textContent = t;
            selectEl.appendChild(opt);
        });
    } catch (err) {
        console.warn('Could not load types:', err);
    }
}
