package com.crimeanalyzer.service;

import com.crimeanalyzer.model.*;
import com.crimeanalyzer.repository.CrimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics engine for the Chennai Crime Pattern Analyzer.
 *
 * <p>Demonstrates heavy use of the Java Collections Framework:
 * <ul>
 *   <li>{@code HashMap<String, Long>} — area/type frequency maps (O(1) avg lookup)</li>
 *   <li>{@code TreeMap<Integer, Long>} — hour/month maps (sorted by key, O(log n) ops)</li>
 *   <li>{@code ArrayList}           — ranked lists (O(1) add, O(n log n) sort)</li>
 *   <li>Java Streams                — aggregation and transformation pipelines</li>
 * </ul>
 *
 * <p>Algorithm complexities documented per method.
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    // Chennai area → approximate coordinates (for hotspot map markers)
    private static final Map<String, double[]> AREA_COORDS = new HashMap<>();
    static {
        AREA_COORDS.put("Anna Nagar",     new double[]{13.0850, 80.2101});
        AREA_COORDS.put("T Nagar",        new double[]{13.0418, 80.2341});
        AREA_COORDS.put("Adyar",          new double[]{13.0012, 80.2565});
        AREA_COORDS.put("Velachery",      new double[]{12.9815, 80.2180});
        AREA_COORDS.put("Tambaram",       new double[]{12.9249, 80.1000});
        AREA_COORDS.put("Guindy",         new double[]{13.0067, 80.2206});
        AREA_COORDS.put("Egmore",         new double[]{13.0732, 80.2609});
        AREA_COORDS.put("Mylapore",       new double[]{13.0368, 80.2676});
        AREA_COORDS.put("Perambur",       new double[]{13.1142, 80.2329});
        AREA_COORDS.put("Ambattur",       new double[]{13.1143, 80.1548});
        AREA_COORDS.put("Porur",          new double[]{13.0359, 80.1573});
        AREA_COORDS.put("Sholinganallur", new double[]{12.9010, 80.2279});
        AREA_COORDS.put("Thoraipakkam",   new double[]{12.9244, 80.2290});
        AREA_COORDS.put("Chromepet",      new double[]{12.9516, 80.1439});
        AREA_COORDS.put("Pallavaram",     new double[]{12.9675, 80.1491});
        AREA_COORDS.put("Saidapet",       new double[]{13.0206, 80.2248});
        AREA_COORDS.put("Nungambakkam",   new double[]{13.0569, 80.2425});
        AREA_COORDS.put("Royapettah",     new double[]{13.0524, 80.2606});
        AREA_COORDS.put("Triplicane",     new double[]{13.0587, 80.2771});
        AREA_COORDS.put("Kodambakkam",    new double[]{13.0526, 80.2225});
        AREA_COORDS.put("Ashok Nagar",    new double[]{13.0296, 80.2136});
        AREA_COORDS.put("Vadapalani",     new double[]{13.0501, 80.2121});
        AREA_COORDS.put("Mogappair",      new double[]{13.0826, 80.1778});
        AREA_COORDS.put("Avadi",          new double[]{13.1147, 80.0982});
        AREA_COORDS.put("Red Hills",      new double[]{13.1878, 80.1792});
    }

    @Autowired
    private CrimeRepository crimeRepository;

    // -------------------------------------------------------
    // Dashboard summary
    // Complexity: O(n) — single pass through aggregation queries
    // -------------------------------------------------------

    public CrimeStatistics getOverallStatistics() {
        // HashMap<String, Long> — area → crime count
        Map<String, Long> byArea = toStringLongMap(crimeRepository.countByArea());

        // HashMap<String, Long> — type → crime count
        Map<String, Long> byType = toStringLongMap(crimeRepository.countByCrimeType());

        // TreeMap<Integer, Long> — month → crime count (sorted by month number)
        Map<Integer, Long> byMonth = toIntLongMap(crimeRepository.countByMonth());

        // TreeMap<Integer, Long> — hour → crime count (sorted by hour)
        Map<Integer, Long> byHour = toIntLongMap(crimeRepository.countByHour());

        // HashMap<String, Long> — dayOfWeek → count
        Map<String, Long> byDay = toStringLongMap(crimeRepository.countByDayOfWeek());

        // TreeMap<Integer, Long> — year → count
        Map<Integer, Long> byYear = toIntLongMap(crimeRepository.countByYear());

        // HashMap<String, Long> — severity → count
        Map<String, Long> bySeverity = toStringLongMap(crimeRepository.countBySeverity());

        // HashMap<String, Long> — status → count
        Map<String, Long> byStatus = toStringLongMap(crimeRepository.countByStatus());

        // Most frequent crime type (max value in HashMap — O(k))
        Map.Entry<String, Long> topType = byType.entrySet().stream()
            .max(Map.Entry.comparingByValue()).orElse(null);

        // Most active area (max in HashMap — O(k))
        Map.Entry<String, Long> topArea = byArea.entrySet().stream()
            .max(Map.Entry.comparingByValue()).orElse(null);

        return CrimeStatistics.builder()
            .totalRecords(crimeRepository.count())
            .highSeverityCount(bySeverity.getOrDefault("HIGH", 0L))
            .mediumSeverityCount(bySeverity.getOrDefault("MEDIUM", 0L))
            .lowSeverityCount(bySeverity.getOrDefault("LOW", 0L))
            .totalAreas(byArea.size())
            .mostFrequentCrimeType(topType != null ? topType.getKey() : "N/A")
            .mostFrequentCrimeTypeCount(topType != null ? topType.getValue() : 0L)
            .mostActiveArea(topArea != null ? topArea.getKey() : "N/A")
            .mostActiveAreaCount(topArea != null ? topArea.getValue() : 0L)
            .crimesByArea(byArea)
            .crimesByType(byType)
            .crimesByMonth(byMonth)
            .crimesByHour(byHour)
            .crimesByDayOfWeek(byDay)
            .crimesByYear(byYear)
            .severityDistribution(bySeverity)
            .statusDistribution(byStatus)
            .build();
    }

    // -------------------------------------------------------
    // Per-area analysis
    // Complexity: O(n) per area — streams over area-filtered records
    // -------------------------------------------------------

    public AreaAnalysis analyzeArea(String area) {
        List<CrimeRecord> records = crimeRepository.findByAreaIgnoreCase(area);
        if (records.isEmpty()) return null;

        // HashMap<String, Long> — crimeType → count
        Map<String, Long> crimeTypeMap = new HashMap<>();
        // TreeMap<Integer, Long> — month → count (auto-sorted)
        Map<Integer, Long> monthlyMap  = new TreeMap<>();
        // TreeMap<Integer, Long> — hour → count (auto-sorted)
        Map<Integer, Long> hourlyMap   = new TreeMap<>();
        // HashMap<String, Long> — severity → count
        Map<String, Long> severityMap  = new HashMap<>();
        // HashMap<String, Long> — dayOfWeek → count
        Map<String, Long> dayMap       = new HashMap<>();

        // O(n) single pass using Java Streams
        records.forEach(r -> {
            crimeTypeMap.merge(r.getCrimeType(), 1L, Long::sum);
            monthlyMap.merge(r.getMonth(), 1L, Long::sum);
            hourlyMap.merge(r.getHour(), 1L, Long::sum);
            severityMap.merge(r.getSeverity(), 1L, Long::sum);
            dayMap.merge(r.getDayOfWeek(), 1L, Long::sum);
        });

        // Most common crime type — O(k) max scan
        String topType = crimeTypeMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("N/A");
        long topTypeCount = crimeTypeMap.getOrDefault(topType, 0L);

        // Peak hour — O(k) max scan on TreeMap
        int peakHour = hourlyMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(0);

        String peakWindow = String.format("%02d:00 – %02d:00", peakHour, (peakHour + 1) % 24);

        // Dominant severity — O(k) max scan
        String dominantSeverity = severityMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("N/A");

        // Most active day — O(k) max scan
        String topDay = dayMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("N/A");

        String zone = records.get(0).getZone();
        double hotspotScore = computeAreaHotspotScore(area, records);

        return AreaAnalysis.builder()
            .areaName(area)
            .zone(zone)
            .totalRecords(records.size())
            .mostCommonCrimeType(topType)
            .mostCommonCrimeCount(topTypeCount)
            .dominantSeverity(dominantSeverity)
            .peakHourRange(peakWindow)
            .peakHour(peakHour)
            .mostActiveDayOfWeek(topDay)
            .monthlyTrend(monthlyMap)
            .hourlyTrend(hourlyMap)
            .crimeTypeDistribution(crimeTypeMap)
            .severityDistribution(severityMap)
            .dayOfWeekDistribution(dayMap)
            .hotspotScore(hotspotScore)
            .hotspotLevel(classifyHotspot(hotspotScore))
            .build();
    }

    // -------------------------------------------------------
    // Hotspot analysis
    // Complexity: O(n) for frequency calc + O(k log k) for sort
    // -------------------------------------------------------

    /**
     * Computes hotspot scores for all areas using the composite formula:
     * <pre>
     *   score = freq * 0.5 + severity * 0.3 + recency * 0.2
     * </pre>
     * Uses ArrayList for the result list and HashMap for intermediate maps.
     *
     * @return list of {@link HotspotResult} sorted by score descending
     */
    public List<HotspotResult> computeHotspots() {
        // Step 1: Get raw counts from DB — O(n) aggregate
        List<Object[]> areaCounts    = crimeRepository.countByArea();   // [area, count]
        List<Object[]> severityData  = crimeRepository.countBySeverity(); // [sev, count]
        List<Object[]> typeCounts    = crimeRepository.countByCrimeType(); // [type, count]

        // Step 2: Build HashMap<String, Long> for counts — O(k)
        Map<String, Long> areaFreqMap = toStringLongMap(areaCounts);

        // Step 3: Find max for normalization — O(k)
        long maxFreq = areaFreqMap.values().stream().mapToLong(v -> v).max().orElse(1);

        // Step 4: Dominant crime type per area — O(n) with HashMap
        Map<String, String> areaDominantType = new HashMap<>();
        typeCounts.stream().findFirst()
            .ifPresent(r -> areaDominantType.put("global", r[0].toString()));

        // Step 5: Build HotspotResult list using ArrayList — O(k)
        List<HotspotResult> results = new ArrayList<>();

        for (Map.Entry<String, Long> entry : areaFreqMap.entrySet()) {
            String areaName = entry.getKey();
            long freq       = entry.getValue();

            double normFreq = (double) freq / maxFreq;

            // Severity score: weighted HIGH=3, MEDIUM=2, LOW=1
            long highCount   = crimeRepository.countHighSeverityByArea(areaName);
            double normSev   = Math.min(1.0, (double) highCount / Math.max(1, freq) * 3.0);

            // Recency: crimes in the most recent year
            int currentYear  = java.time.LocalDate.now().getYear();
            long recentCount = crimeRepository.countByAreaAndYear(areaName, currentYear);
            double normRecent = Math.min(1.0, (double) recentCount / Math.max(1, freq) * 2.0);

            double score = (normFreq * 0.5) + (normSev * 0.3) + (normRecent * 0.2);

            double[] coords = AREA_COORDS.getOrDefault(areaName, new double[]{13.0827, 80.2707});

            results.add(HotspotResult.builder()
                .area(areaName)
                .zone(getZoneForArea(areaName))
                .totalCrimes(freq)
                .normalizedFrequency(round(normFreq))
                .normalizedSeverityScore(round(normSev))
                .normalizedRecentActivity(round(normRecent))
                .hotspotScore(round(score))
                .hotspotLevel(classifyHotspot(score))
                .latitude(coords[0])
                .longitude(coords[1])
                .dominantCrimeType(areaDominantType.getOrDefault(areaName, "Theft"))
                .build());
        }

        // O(k log k) sort by score descending
        results.sort(Comparator.comparingDouble(HotspotResult::getHotspotScore).reversed());
        return results;
    }

    // -------------------------------------------------------
    // Distribution getters (for individual API endpoints)
    // -------------------------------------------------------

    /** Returns crime count by area as a sorted HashMap. Complexity: O(n) */
    public Map<String, Long> getCrimesByArea() {
        return toStringLongMap(crimeRepository.countByArea());
    }

    /** Returns crime count by type. Complexity: O(n) */
    public Map<String, Long> getCrimesByType() {
        return toStringLongMap(crimeRepository.countByCrimeType());
    }

    /** Returns crime count by month via TreeMap (sorted). Complexity: O(n) */
    public Map<Integer, Long> getCrimesByMonth() {
        return toIntLongMap(crimeRepository.countByMonth());
    }

    /** Returns crime count by hour via TreeMap (sorted). Complexity: O(n) */
    public Map<Integer, Long> getCrimesByHour() {
        return toIntLongMap(crimeRepository.countByHour());
    }

    // -------------------------------------------------------
    // Hotspot score helpers
    // -------------------------------------------------------

    private double computeAreaHotspotScore(String area, List<CrimeRecord> records) {
        if (records.isEmpty()) return 0.0;
        long total = crimeRepository.count();
        double normFreq  = Math.min(1.0, (double) records.size() / Math.max(1, total / 25.0));
        long highCount   = records.stream().filter(r -> "HIGH".equals(r.getSeverity())).count();
        double normSev   = Math.min(1.0, (double) highCount / records.size() * 3.0);
        int yr           = java.time.LocalDate.now().getYear();
        long recent      = records.stream().filter(r -> r.getYear() == yr).count();
        double normRec   = Math.min(1.0, (double) recent / Math.max(1, records.size()) * 2.0);
        return round((normFreq * 0.5) + (normSev * 0.3) + (normRec * 0.2));
    }

    public String classifyHotspot(double score) {
        if (score >= 0.60) return "HIGH";
        if (score >= 0.35) return "MODERATE";
        return "LOW";
    }

    // -------------------------------------------------------
    // Internal utilities
    // -------------------------------------------------------

    /**
     * Converts a list of Object[] pairs [String, Number] into a HashMap.
     * Complexity: O(k) where k = number of distinct groups.
     */
    private Map<String, Long> toStringLongMap(List<Object[]> rows) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return map;
    }

    /**
     * Converts [Integer, Number] pairs into a TreeMap (sorted by key).
     * Complexity: O(k log k) due to TreeMap insertions.
     */
    private Map<Integer, Long> toIntLongMap(List<Object[]> rows) {
        Map<Integer, Long> map = new TreeMap<>();
        for (Object[] row : rows) {
            map.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return map;
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private String getZoneForArea(String area) {
        Map<String, String> zones = new HashMap<>();
        zones.put("Anna Nagar",     "North");
        zones.put("T Nagar",        "Central");
        zones.put("Adyar",          "South");
        zones.put("Velachery",      "South");
        zones.put("Tambaram",       "South West");
        zones.put("Guindy",         "Central");
        zones.put("Egmore",         "Central");
        zones.put("Mylapore",       "Central");
        zones.put("Perambur",       "North");
        zones.put("Ambattur",       "North West");
        zones.put("Porur",          "West");
        zones.put("Sholinganallur", "South East");
        zones.put("Thoraipakkam",   "South East");
        zones.put("Chromepet",      "South West");
        zones.put("Pallavaram",     "South West");
        zones.put("Saidapet",       "Central");
        zones.put("Nungambakkam",   "Central");
        zones.put("Royapettah",     "Central");
        zones.put("Triplicane",     "Central");
        zones.put("Kodambakkam",    "West Central");
        zones.put("Ashok Nagar",    "Central");
        zones.put("Vadapalani",     "West Central");
        zones.put("Mogappair",      "North West");
        zones.put("Avadi",          "North West");
        zones.put("Red Hills",      "North");
        return zones.getOrDefault(area, "Central");
    }
}
