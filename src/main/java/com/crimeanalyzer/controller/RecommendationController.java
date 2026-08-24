package com.crimeanalyzer.controller;

import com.crimeanalyzer.model.Recommendation;
import com.crimeanalyzer.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for crime prevention recommendations.
 *
 * <p>Base path: {@code /api/recommendations}
 *
 * <p><strong>DISCLAIMER:</strong> All recommendations are generic, preventive,
 * and location-level. They are based on synthetic academic data only and
 * do NOT profile individuals or represent law-enforcement guidance.
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Returns supported crime types and areas for dropdowns.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("supportedCrimeTypes", List.of(
            "Theft", "Burglary", "Robbery", "Vehicle Theft", "Cyber Crime",
            "Fraud", "Assault", "Vandalism", "Chain Snatching", "Harassment"
        ));
        meta.put("disclaimer",
            "Recommendations are generic and preventive. Based on synthetic data only.");
        return ResponseEntity.ok(meta);
    }

    /**
     * Returns a recommendation for a specific area and crime type.
     *
     * @param area      Chennai area name (URL-encoded)
     * @param crimeType crime type (URL-encoded)
     */
    @GetMapping("/{area}/{crimeType}")
    public ResponseEntity<Recommendation> getRecommendation(
            @PathVariable String area,
            @PathVariable String crimeType) {
        Recommendation rec = recommendationService.getRecommendation(area, crimeType);
        return ResponseEntity.ok(rec);
    }

    /**
     * Returns recommendations for all crime types for an area.
     *
     * @param area Chennai area name
     */
    @GetMapping("/area/{area}")
    public ResponseEntity<List<Recommendation>> getForArea(@PathVariable String area) {
        return ResponseEntity.ok(recommendationService.getRecommendationsForArea(area));
    }
}
