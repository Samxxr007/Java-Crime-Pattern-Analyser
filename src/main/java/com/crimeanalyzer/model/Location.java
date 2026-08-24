package com.crimeanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a geographical location in Chennai with its coordinates and zone.
 *
 * <p>Demonstrates encapsulation by grouping location-related data
 * into a single cohesive object. Used by the {@code DatasetGenerator}
 * and the frontend map visualization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    private String areaName;
    private String zone;
    private double latitude;
    private double longitude;

    /**
     * Returns a formatted display string for the area.
     * Demonstrates method encapsulation within a model object.
     */
    public String getDisplayLabel() {
        return areaName + " (" + zone + ")";
    }

    /**
     * Returns approximate distance (in km) to another location
     * using the Haversine formula. Useful for clustering.
     *
     * <p>Complexity: O(1) — constant-time trigonometric calculation.
     *
     * @param other the target location
     * @return distance in kilometres
     */
    public double distanceTo(Location other) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(this.latitude))
                 * Math.cos(Math.toRadians(other.latitude))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
