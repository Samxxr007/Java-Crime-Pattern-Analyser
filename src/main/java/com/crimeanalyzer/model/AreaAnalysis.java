package com.crimeanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Detailed analysis result for a single Chennai area.
 *
 * <p>Populated by {@code AnalyticsService#analyzeArea()} which uses
 * Java Streams, HashMap, and TreeMap to compute distributions from the
 * synthetic dataset.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaAnalysis {

    private String areaName;
    private String zone;
    private long totalRecords;

    private String mostCommonCrimeType;
    private long mostCommonCrimeCount;

    private String dominantSeverity;
    private String peakHourRange;
    private int peakHour;

    private String mostActiveDayOfWeek;

    /** month (1–12) → count  [TreeMap — sorted by month] */
    private Map<Integer, Long> monthlyTrend;

    /** hour (0–23) → count   [TreeMap — sorted by hour]  */
    private Map<Integer, Long> hourlyTrend;

    /** crimeType → count */
    private Map<String, Long> crimeTypeDistribution;

    /** severity → count */
    private Map<String, Long> severityDistribution;

    /** dayOfWeek → count */
    private Map<String, Long> dayOfWeekDistribution;

    /** Hotspot classification: LOW | MODERATE | HIGH */
    private String hotspotLevel;

    /** Normalised composite hotspot score (0.0–1.0) */
    private double hotspotScore;
}
