package com.crimeanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Aggregated statistics for the dashboard and API responses.
 *
 * <p>This POJO is populated by {@code AnalyticsService} using the
 * Java Collections Framework and Streams API. It is returned as a
 * JSON payload to the frontend dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrimeStatistics {

    // -------------------------------------------------------
    // Summary totals
    // -------------------------------------------------------
    private long totalRecords;
    private long highSeverityCount;
    private long mediumSeverityCount;
    private long lowSeverityCount;
    private int totalAreas;

    private String mostFrequentCrimeType;
    private long mostFrequentCrimeTypeCount;
    private String mostActiveArea;
    private long mostActiveAreaCount;

    // -------------------------------------------------------
    // Distribution maps  (key → count)
    // Backed by HashMap<String,Long> — average O(1) lookup
    // -------------------------------------------------------

    /** Crime count keyed by area name. */
    private Map<String, Long> crimesByArea;

    /** Crime count keyed by crime type. */
    private Map<String, Long> crimesByType;

    /** Crime count keyed by month number (1–12). */
    private Map<Integer, Long> crimesByMonth;

    /** Crime count keyed by hour of day (0–23). */
    private Map<Integer, Long> crimesByHour;

    /** Crime count keyed by day-of-week name. */
    private Map<String, Long> crimesByDayOfWeek;

    /** Crime count keyed by year. */
    private Map<Integer, Long> crimesByYear;

    /** Severity label → count. */
    private Map<String, Long> severityDistribution;

    /** Case status → count. */
    private Map<String, Long> statusDistribution;
}
