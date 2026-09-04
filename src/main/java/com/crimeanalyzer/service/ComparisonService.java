package com.crimeanalyzer.service;

import com.crimeanalyzer.model.AreaAnalysis;
import com.crimeanalyzer.model.AreaComparison;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for side-by-side comparative analysis between two Chennai areas.
 */
@Service
public class ComparisonService {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Generates a comparative analysis DTO between area1 and area2.
     *
     * @param area1 First area name
     * @param area2 Second area name
     * @return AreaComparison containing comparative metrics
     */
    public AreaComparison compareAreas(String area1, String area2) {
        AreaAnalysis analysis1 = analyticsService.analyzeArea(area1);
        AreaAnalysis analysis2 = analyticsService.analyzeArea(area2);

        if (analysis1 == null || analysis2 == null) {
            throw new IllegalArgumentException("One or both areas could not be found or have no incident records.");
        }

        double csi1 = calculateCrimeSeverityIndex(analysis1);
        double csi2 = calculateCrimeSeverityIndex(analysis2);

        int safetyScore1 = calculateSafetyScore(analysis1, csi1);
        int safetyScore2 = calculateSafetyScore(analysis2, csi2);

        String summary = generateComparativeSummary(area1, area2, analysis1, analysis2, safetyScore1, safetyScore2);

        return AreaComparison.builder()
                .area1(area1)
                .area2(area2)
                .totalIncidentsArea1(analysis1.getTotalRecords())
                .totalIncidentsArea2(analysis2.getTotalRecords())
                .crimeSeverityIndexArea1(csi1)
                .crimeSeverityIndexArea2(csi2)
                .safetyScoreArea1(safetyScore1)
                .safetyScoreArea2(safetyScore2)
                .dominantCrimeArea1(analysis1.getMostCommonCrimeType())
                .dominantCrimeArea2(analysis2.getMostCommonCrimeType())
                .peakTimeArea1(analysis1.getPeakHourRange())
                .peakTimeArea2(analysis2.getPeakHourRange())
                .hotspotLevelArea1(analysis1.getHotspotLevel())
                .hotspotLevelArea2(analysis2.getHotspotLevel())
                .hotspotScoreArea1(analysis1.getHotspotScore())
                .hotspotScoreArea2(analysis2.getHotspotScore())
                .crimeDistributionArea1(analysis1.getCrimeTypeDistribution())
                .crimeDistributionArea2(analysis2.getCrimeTypeDistribution())
                .severityDistributionArea1(analysis1.getSeverityDistribution())
                .severityDistributionArea2(analysis2.getSeverityDistribution())
                .comparativeSummary(summary)
                .build();
    }

    /**
     * Crime Severity Index (CSI) formula:
     * Weighted sum: HIGH=3.0, MEDIUM=2.0, LOW=1.0 divided by total count.
     */
    private double calculateCrimeSeverityIndex(AreaAnalysis analysis) {
        Map<String, Long> sev = analysis.getSeverityDistribution();
        long high = sev.getOrDefault("HIGH", 0L);
        long med = sev.getOrDefault("MEDIUM", 0L);
        long low = sev.getOrDefault("LOW", 0L);
        long total = analysis.getTotalRecords();

        if (total == 0) return 0.0;
        double score = (high * 3.0 + med * 2.0 + low * 1.0) / total;
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Safety Score (0-100 scale, higher is safer):
     * Derived from hotspot score and severity index.
     */
    private int calculateSafetyScore(AreaAnalysis analysis, double csi) {
        double penalty = (analysis.getHotspotScore() * 50.0) + (csi / 3.0 * 40.0);
        int score = (int) Math.round(100.0 - penalty);
        return Math.max(15, Math.min(95, score));
    }

    private String generateComparativeSummary(String a1, String a2, AreaAnalysis an1, AreaAnalysis an2, int s1, int s2) {
        String saferArea = s1 >= s2 ? a1 : a2;
        String higherRiskArea = s1 >= s2 ? a2 : a1;
        int diff = Math.abs(s1 - s2);

        return String.format(
                "%s registers a safety score of %d/100 compared to %d/100 for %s (a differential of %d points). " +
                        "%s records %d total incidents primarily driven by %s, while %s records %d incidents with %s as the primary category.",
                saferArea, Math.max(s1, s2), Math.min(s1, s2), higherRiskArea, diff,
                a1, an1.getTotalRecords(), an1.getMostCommonCrimeType(),
                a2, an2.getTotalRecords(), an2.getMostCommonCrimeType()
        );
    }
}
