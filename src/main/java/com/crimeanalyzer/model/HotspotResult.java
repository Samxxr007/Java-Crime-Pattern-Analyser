package com.crimeanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a hotspot result for an area with its composite score and classification.
 *
 * <p>The hotspot score formula:
 * <pre>
 *   hotspotScore = normalizedFrequency * 0.5
 *                + normalizedSeverityScore * 0.3
 *                + normalizedRecentActivity * 0.2
 * </pre>
 *
 * <strong>Academic note:</strong> This is a synthetic statistical score based on
 * generated data. It does NOT predict actual future crime or represent official data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotspotResult {

    private String area;
    private String zone;
    private long totalCrimes;
    private double normalizedFrequency;
    private double normalizedSeverityScore;
    private double normalizedRecentActivity;

    /** Composite hotspot score in range [0.0, 1.0]. */
    private double hotspotScore;

    /** Classification: LOW | MODERATE | HIGH */
    private String hotspotLevel;

    private double latitude;
    private double longitude;

    /** Most common crime type for this area. */
    private String dominantCrimeType;
}
