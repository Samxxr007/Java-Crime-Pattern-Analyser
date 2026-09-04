package com.crimeanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing spatial proximity scan results within a radius.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProximityResult {

    private double centerLatitude;
    private double centerLongitude;
    private double radiusKm;
    private long totalIncidents;
    private double averageDistanceKm;
    private String nearestIncidentArea;
    private Map<String, Long> crimeTypeBreakdown;
    private Map<String, Long> severityBreakdown;
    private List<ProximityIncident> incidents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProximityIncident {
        private String crimeId;
        private String crimeDate;
        private String crimeTime;
        private String area;
        private String crimeType;
        private String severity;
        private double latitude;
        private double longitude;
        private double distanceKm;
        private String caseStatus;
    }
}
