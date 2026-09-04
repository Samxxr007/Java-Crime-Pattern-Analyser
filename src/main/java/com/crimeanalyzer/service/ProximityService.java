package com.crimeanalyzer.service;

import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.model.Location;
import com.crimeanalyzer.model.ProximityResult;
import com.crimeanalyzer.repository.CrimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service providing spatial proximity radius scanning using the Haversine Great-Circle formula.
 */
@Service
public class ProximityService {

    @Autowired
    private CrimeRepository crimeRepository;

    /**
     * Finds all incidents within a specified radius (in kilometers) from a center coordinate.
     *
     * @param centerLat Center latitude
     * @param centerLon Center longitude
     * @param radiusKm Radius in kilometers
     * @return ProximityResult containing metrics and sorted incidents
     */
    public ProximityResult findIncidentsWithinRadius(double centerLat, double centerLon, double radiusKm) {
        Location center = new Location("Query Center", "Target", centerLat, centerLon);

        List<CrimeRecord> allRecords = crimeRepository.findAll();

        List<ProximityResult.ProximityIncident> matching = new ArrayList<>();
        double totalDistance = 0.0;

        for (CrimeRecord record : allRecords) {
            if (record.getLatitude() != null && record.getLongitude() != null) {
                Location incidentLoc = new Location(record.getArea(), record.getZone(),
                        record.getLatitude(), record.getLongitude());
                double dist = center.distanceTo(incidentLoc);

                if (dist <= radiusKm) {
                    matching.add(ProximityResult.ProximityIncident.builder()
                            .crimeId(record.getCrimeId())
                            .crimeDate(record.getCrimeDate() != null ? record.getCrimeDate().toString() : "")
                            .crimeTime(record.getCrimeTime() != null ? record.getCrimeTime().toString() : "")
                            .area(record.getArea())
                            .crimeType(record.getCrimeType())
                            .severity(record.getSeverity())
                            .latitude(record.getLatitude())
                            .longitude(record.getLongitude())
                            .distanceKm(Math.round(dist * 100.0) / 100.0)
                            .caseStatus(record.getCaseStatus())
                            .build());
                    totalDistance += dist;
                }
            }
        }

        // Sort ascending by distance (closest first)
        matching.sort(Comparator.comparingDouble(ProximityResult.ProximityIncident::getDistanceKm));

        Map<String, Long> crimeTypeBreakdown = matching.stream()
                .collect(Collectors.groupingBy(ProximityResult.ProximityIncident::getCrimeType, Collectors.counting()));

        Map<String, Long> severityBreakdown = matching.stream()
                .collect(Collectors.groupingBy(ProximityResult.ProximityIncident::getSeverity, Collectors.counting()));

        double avgDist = matching.isEmpty() ? 0.0 : Math.round((totalDistance / matching.size()) * 100.0) / 100.0;
        String nearestArea = matching.isEmpty() ? "None" : matching.get(0).getArea();

        return ProximityResult.builder()
                .centerLatitude(centerLat)
                .centerLongitude(centerLon)
                .radiusKm(radiusKm)
                .totalIncidents(matching.size())
                .averageDistanceKm(avgDist)
                .nearestIncidentArea(nearestArea)
                .crimeTypeBreakdown(crimeTypeBreakdown)
                .severityBreakdown(severityBreakdown)
                .incidents(matching)
                .build();
    }
}
