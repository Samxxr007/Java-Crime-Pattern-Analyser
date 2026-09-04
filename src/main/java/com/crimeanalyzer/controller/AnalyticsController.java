package com.crimeanalyzer.controller;

import com.crimeanalyzer.model.*;
import com.crimeanalyzer.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for analytics and hotspot endpoints.
 *
 * <p>Base path: {@code /api/analytics}
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /** Full dashboard statistics summary. */
    @GetMapping("/summary")
    public ResponseEntity<CrimeStatistics> getSummary() {
        return ResponseEntity.ok(analyticsService.getOverallStatistics());
    }

    /** Crime count grouped by area. */
    @GetMapping("/by-area")
    public ResponseEntity<Map<String, Long>> byArea() {
        return ResponseEntity.ok(analyticsService.getCrimesByArea());
    }

    /** Crime count grouped by crime type. */
    @GetMapping("/by-type")
    public ResponseEntity<Map<String, Long>> byType() {
        return ResponseEntity.ok(analyticsService.getCrimesByType());
    }

    /** Crime count grouped by month. */
    @GetMapping("/by-month")
    public ResponseEntity<Map<Integer, Long>> byMonth() {
        return ResponseEntity.ok(analyticsService.getCrimesByMonth());
    }

    /** Crime count grouped by hour of day. */
    @GetMapping("/by-hour")
    public ResponseEntity<Map<Integer, Long>> byHour() {
        return ResponseEntity.ok(analyticsService.getCrimesByHour());
    }

    /** Hotspot scores for all areas. */
    @GetMapping("/hotspots")
    public ResponseEntity<List<HotspotResult>> hotspots() {
        return ResponseEntity.ok(analyticsService.computeHotspots());
    }

    @Autowired
    private com.crimeanalyzer.service.ComparisonService comparisonService;

    @Autowired
    private com.crimeanalyzer.service.ProximityService proximityService;

    /** Side-by-side comparison between two areas. */
    @GetMapping("/compare")
    public ResponseEntity<AreaComparison> compareAreas(
            @RequestParam String area1,
            @RequestParam String area2) {
        return ResponseEntity.ok(comparisonService.compareAreas(area1, area2));
    }

    /** Proximity radius scan using Haversine distance. */
    @GetMapping("/proximity")
    public ResponseEntity<ProximityResult> proximityScan(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "3.0") double radiusKm) {
        return ResponseEntity.ok(proximityService.findIncidentsWithinRadius(lat, lon, radiusKm));
    }

    /** Recent simulated incidents for live command center feed. */
    @GetMapping("/live-feed")
    public ResponseEntity<List<CrimeRecord>> getLiveFeed() {
        return ResponseEntity.ok(analyticsService.getLiveFeed());
    }

    /** Detailed analysis for a specific area. */
    @GetMapping("/area/{area}")
    public ResponseEntity<AreaAnalysis> areaAnalysis(@PathVariable String area) {
        AreaAnalysis analysis = analyticsService.analyzeArea(area);
        if (analysis == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analysis);
    }
}
