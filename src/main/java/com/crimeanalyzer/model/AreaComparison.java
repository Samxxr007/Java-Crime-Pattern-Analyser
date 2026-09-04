package com.crimeanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object representing a side-by-side comparison between two Chennai localities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaComparison {

    private String area1;
    private String area2;

    private long totalIncidentsArea1;
    private long totalIncidentsArea2;

    private double crimeSeverityIndexArea1;
    private double crimeSeverityIndexArea2;

    private int safetyScoreArea1; // 0 to 100
    private int safetyScoreArea2; // 0 to 100

    private String dominantCrimeArea1;
    private String dominantCrimeArea2;

    private String peakTimeArea1;
    private String peakTimeArea2;

    private String hotspotLevelArea1;
    private String hotspotLevelArea2;

    private double hotspotScoreArea1;
    private double hotspotScoreArea2;

    private Map<String, Long> crimeDistributionArea1;
    private Map<String, Long> crimeDistributionArea2;

    private Map<String, Long> severityDistributionArea1;
    private Map<String, Long> severityDistributionArea2;

    private String comparativeSummary;
}
