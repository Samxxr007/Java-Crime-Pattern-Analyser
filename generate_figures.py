import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches
import numpy as np

os.makedirs('figures', exist_ok=True)

# Set global font to Times New Roman
plt.rcParams['font.family'] = 'serif'
plt.rcParams['font.serif'] = ['Times New Roman', 'DejaVu Serif']
plt.rcParams['font.size'] = 10

# -------------------------------------------------------------
# Figure 1: System Architecture Diagram
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(8.5, 5.5), dpi=300)
ax.axis('off')

# Background boxes for layers
layers = [
    ("Presentation Layer (Client Browser)", 0.82, 0.14, "#EBF5FB", "#2980B9"),
    ("Controller & API Layer (Spring Boot REST)", 0.62, 0.14, "#EAFAF1", "#27AE60"),
    ("Service & Analytics Engine Layer", 0.42, 0.14, "#FEF9E7", "#D4AC0D"),
    ("Data Persistence & Repository Layer (Spring Data JPA)", 0.22, 0.14, "#F4ECF7", "#8E44AD"),
    ("Data Storage Layer (MySQL / H2 / Synthetic CSV)", 0.02, 0.14, "#FADBD8", "#C0392B"),
]

for title, y, h, bg, border in layers:
    rect = patches.FancyBboxPatch((0.05, y), 0.90, h, boxstyle="round,pad=0.02",
                                  ec=border, fc=bg, lw=1.5)
    ax.add_patch(rect)
    ax.text(0.08, y + h - 0.035, title, fontsize=11, fontweight='bold', color=border)

# Components in Presentation Layer
p_comps = ["Thymeleaf HTML5 Views", "Bootstrap 5 CSS", "Leaflet.js Map & Heatmap", "Chart.js Visualizations", "Vanilla JS Async Fetch"]
for i, comp in enumerate(p_comps):
    x = 0.08 + i * 0.17
    r = patches.FancyBboxPatch((x, 0.835), 0.155, 0.065, boxstyle="round,pad=0.01", ec="#2980B9", fc="#FFFFFF", lw=1)
    ax.add_patch(r)
    ax.text(x + 0.0775, 0.8675, comp, fontsize=8, ha='center', va='center', color="#1A5276")

# Components in Controller Layer
c_comps = ["PageController\n(View Routing)", "CrimeController\n(CRUD & Filter API)", "AnalyticsController\n(Statistical Aggregation)", "RecommendationController\n(Prevention Recommender)", "GlobalExceptionHandler\n(Error Mapping)"]
for i, comp in enumerate(c_comps):
    x = 0.08 + i * 0.17
    r = patches.FancyBboxPatch((x, 0.635), 0.155, 0.075, boxstyle="round,pad=0.01", ec="#27AE60", fc="#FFFFFF", lw=1)
    ax.add_patch(r)
    ax.text(x + 0.0775, 0.6725, comp, fontsize=7.5, ha='center', va='center', color="#1E8449")

# Components in Service Layer
s_comps = ["CrimeService\n(Validation & Query Logic)", "AnalyticsService\n(HashMap & TreeMap Agg)", "RecommendationService\n(Rule Heuristics & Advice)", "DatasetService\n(CSV Ingestion & Import)", "DatasetGenerator\n(Reproducible Seed Gen)"]
for i, comp in enumerate(s_comps):
    x = 0.08 + i * 0.17
    r = patches.FancyBboxPatch((x, 0.435), 0.155, 0.075, boxstyle="round,pad=0.01", ec="#D4AC0D", fc="#FFFFFF", lw=1)
    ax.add_patch(r)
    ax.text(x + 0.0775, 0.4725, comp, fontsize=7.5, ha='center', va='center', color="#B7950B")

# Components in Repository Layer
r_comps = ["CrimeRepository (JpaRepository)", "Custom JPQL Aggregations", "B-Tree Spatial & Type Indexes", "Entity Models (CrimeRecord, etc.)"]
for i, comp in enumerate(r_comps):
    x = 0.08 + i * 0.215
    r = patches.FancyBboxPatch((x, 0.235), 0.195, 0.07, boxstyle="round,pad=0.01", ec="#8E44AD", fc="#FFFFFF", lw=1)
    ax.add_patch(r)
    ax.text(x + 0.0975, 0.27, comp, fontsize=8, ha='center', va='center', color="#6C3483")

# Components in Storage Layer
d_comps = ["MySQL 8.0 (Production Database)", "H2 In-Memory (Zero-Setup Fallback)", "chennai_crime_synthetic.csv (3000 Records)"]
for i, comp in enumerate(d_comps):
    x = 0.08 + i * 0.29
    r = patches.FancyBboxPatch((x, 0.035), 0.265, 0.07, boxstyle="round,pad=0.01", ec="#C0392B", fc="#FFFFFF", lw=1)
    ax.add_patch(r)
    ax.text(x + 0.1325, 0.07, comp, fontsize=8.5, ha='center', va='center', color="#922B21")

# Connecting Arrows
for y_arr in [0.81, 0.61, 0.41, 0.21]:
    ax.annotate("", xy=(0.5, y_arr - 0.04), xytext=(0.5, y_arr),
                arrowprops=dict(arrowstyle="<->", color="#5D6D7E", lw=1.5))

plt.tight_layout()
plt.savefig('figures/fig1_architecture.png', dpi=300)
plt.close()
print("Figure 1 generated.")

# -------------------------------------------------------------
# Figure 2: Hotspot Scoring Flowchart
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(8, 4.5), dpi=300)
ax.axis('off')

steps = [
    ("Spatial & Incident Query\n(Extract Area Incidents)", 0.05, 0.40, "#E8F8F5", "#1ABC9C"),
    ("Frequency Normalization\n(normFreq = Count / MaxFreq)", 0.25, 0.60, "#EBF5FB", "#3498DB"),
    ("Severity Weighting\n(normSev = HighSev / Count * 3.0)", 0.25, 0.40, "#FEF5E7", "#F39C12"),
    ("Recency Normalization\n(normRec = RecentYr / Count * 2.0)", 0.25, 0.20, "#FDEDEC", "#E74C3C"),
    ("Composite Score Calculation\n(0.5*Freq + 0.3*Sev + 0.2*Rec)", 0.55, 0.40, "#F4ECF7", "#9B59B6"),
    ("Risk Level Classification\nHIGH (>=0.60)\nMODERATE (0.35-0.59)\nLOW (<0.35)", 0.78, 0.35, "#EAFAF1", "#2ECC71"),
]

for title, x, y, bg, border in steps:
    h = 0.18 if "\n\n" not in title else 0.24
    w = 0.18
    r = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.015", ec=border, fc=bg, lw=1.5)
    ax.add_patch(r)
    ax.text(x + w/2, y + h/2, title, fontsize=8, ha='center', va='center', color="#2C3E50", fontweight='bold')

# Connectors
ax.annotate("", xy=(0.25, 0.69), xytext=(0.23, 0.49), arrowprops=dict(arrowstyle="->", color="#34495E", lw=1.2))
ax.annotate("", xy=(0.25, 0.49), xytext=(0.23, 0.49), arrowprops=dict(arrowstyle="->", color="#34495E", lw=1.2))
ax.annotate("", xy=(0.25, 0.29), xytext=(0.23, 0.49), arrowprops=dict(arrowstyle="->", color="#34495E", lw=1.2))

ax.annotate("", xy=(0.55, 0.49), xytext=(0.43, 0.69), arrowprops=dict(arrowstyle="->", color="#34495E", lw=1.2))
ax.annotate("", xy=(0.55, 0.49), xytext=(0.43, 0.49), arrowprops=dict(arrowstyle="->", color="#34495E", lw=1.2))
ax.annotate("", xy=(0.55, 0.49), xytext=(0.43, 0.29), arrowprops=dict(arrowstyle="->", color="#34495E", lw=1.2))

ax.annotate("", xy=(0.78, 0.47), xytext=(0.73, 0.49), arrowprops=dict(arrowstyle="->", color="#34495E", lw=1.5))

plt.tight_layout()
plt.savefig('figures/fig2_hotspot_pipeline.png', dpi=300)
plt.close()
print("Figure 2 generated.")

# -------------------------------------------------------------
# Figure 3: Results Visualizations (4-panel chart)
# -------------------------------------------------------------
fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(9.5, 6.5), dpi=300)

# 1. Top Areas
areas = ['Vadapalani', 'Pallavaram', 'Ashok Nagar', 'Adyar', 'T Nagar', 'Mogappair', 'Perambur', 'Egmore', 'Velachery', 'Porur']
counts = [141, 137, 135, 131, 128, 127, 127, 124, 124, 121]
ax1.barh(areas[::-1], counts[::-1], color='#3498DB', edgecolor='#2980B9', height=0.65)
ax1.set_title('(a) Incident Frequency by Top 10 Areas', fontsize=10, fontweight='bold')
ax1.set_xlabel('Synthetic Record Count', fontsize=9)
ax1.grid(axis='x', linestyle='--', alpha=0.5)

# 2. Crime by Type
types = ['Theft', 'Vehicle Theft', 'Cyber Crime', 'Fraud', 'Burglary', 'Robbery', 'Assault', 'Chain Snatch', 'Vandalism', 'Harassment']
type_counts = [753, 482, 339, 284, 265, 249, 199, 180, 169, 80]
colors = ['#E74C3C', '#E67E22', '#F1C40F', '#2ECC71', '#1ABC9C', '#3498DB', '#9B59B6', '#34495E', '#95A5A6', '#D35400']
ax2.pie(type_counts, labels=types, colors=colors, autopct='%1.1f%%', startangle=140, textprops={'fontsize': 7.5})
ax2.set_title('(b) Incident Distribution by Crime Category', fontsize=10, fontweight='bold')

# 3. Hourly Histogram
hours = list(range(24))
hour_counts = [174, 166, 176, 74, 88, 74, 91, 88, 80, 74, 68, 73, 71, 81, 67, 83, 65, 253, 238, 243, 230, 246, 92, 105]
bar_colors = ['#E74C3C' if c > 200 else '#3498DB' for c in hour_counts]
ax3.bar(hours, hour_counts, color=bar_colors, edgecolor='#1B4F72', width=0.8)
ax3.set_title('(c) Diurnal Incident Distribution (Hourly Peak at 17:00-21:00)', fontsize=10, fontweight='bold')
ax3.set_xlabel('Hour of Day (00:00 - 23:00)', fontsize=9)
ax3.set_ylabel('Incident Count', fontsize=9)
ax3.set_xticks(range(0, 24, 2))
ax3.grid(axis='y', linestyle='--', alpha=0.5)

# 4. Severity Distribution
sev_labels = ['Low Severity', 'Medium Severity', 'High Severity']
sev_counts = [1222, 1190, 588]
sev_colors = ['#2ECC71', '#F39C12', '#E74C3C']
ax4.bar(sev_labels, sev_counts, color=sev_colors, edgecolor='#333333', width=0.5)
ax4.set_title('(d) Incident Breakdown by Severity Level', fontsize=10, fontweight='bold')
ax4.set_ylabel('Incident Count', fontsize=9)
for i, v in enumerate(sev_counts):
    ax4.text(i, v + 25, f"{v} ({v/3000*100:.1f}%)", ha='center', fontsize=8.5, fontweight='bold')
ax4.set_ylim(0, 1450)
ax4.grid(axis='y', linestyle='--', alpha=0.5)

plt.tight_layout()
plt.savefig('figures/fig3_results_charts.png', dpi=300)
plt.close()
print("Figure 3 generated.")

# -------------------------------------------------------------
# Figure 4: Gantt Chart for Project Management
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(9, 4), dpi=300)

tasks = [
    "Requirements & Domain Research",
    "Architecture Design & JPA Modeling",
    "Dataset Synthesis (3000 Records)",
    "Service & Analytics Algorithms",
    "Recommendation Engine Implementation",
    "Web UI, Leaflet Map & Chart.js",
    "Unit Testing & Quality Verification",
    "Documentation & Capstone Report"
]
start_weeks = [1, 2, 4, 5, 7, 8, 10, 11]
durations   = [2, 3, 2, 3, 2, 3,  2,  2]

y_pos = np.arange(len(tasks))
ax.barh(y_pos, durations, left=start_weeks, height=0.55, align='center', color='#2980B9', edgecolor='#1B4F72')

ax.set_yticks(y_pos)
ax.set_yticklabels(tasks, fontsize=9)
ax.invert_yaxis()  # top-down
ax.set_xlabel('Project Execution Timeline (Weeks)', fontsize=9.5, fontweight='bold')
ax.set_title('Capstone Project Execution Gantt Chart (12-Week Lifecycle)', fontsize=11, fontweight='bold')
ax.set_xlim(0, 14)
ax.set_xticks(range(1, 14))
ax.grid(axis='x', linestyle='--', alpha=0.6)

plt.tight_layout()
plt.savefig('figures/fig4_gantt_chart.png', dpi=300)
plt.close()
print("Figure 4 generated.")
