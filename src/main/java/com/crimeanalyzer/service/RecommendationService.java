package com.crimeanalyzer.service;

import com.crimeanalyzer.model.*;
import com.crimeanalyzer.repository.CrimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates location-level, preventive crime recommendations based on
 * synthetic crime pattern data.
 *
 * <p><strong>Design principle (OOP — Strategy pattern):</strong>
 * Recommendations are selected from a {@code HashMap<String, List<String>>}
 * keyed by crime type, enabling O(1) lookup of relevant measures.
 *
 * <p><strong>IMPORTANT:</strong> All recommendations are:
 * <ul>
 *   <li>Generic and preventive in nature</li>
 *   <li>Location-level (not individual profiling)</li>
 *   <li>Based on synthetic academic data only</li>
 *   <li>Not prescriptive law-enforcement guidance</li>
 * </ul>
 */
@Service
public class RecommendationService {

    // HashMap<String, List<String>> — O(1) lookup by crime type
    private static final Map<String, List<String>> MEASURES_BY_TYPE = new HashMap<>();

    // HashMap<String, String> — general advisory text per crime type
    private static final Map<String, String> ADVISORY_BY_TYPE = new HashMap<>();

    static {
        MEASURES_BY_TYPE.put("Theft", List.of(
            "Improve CCTV surveillance coverage in commercial and public areas",
            "Enhance street lighting especially in markets and pedestrian zones",
            "Encourage use of secure storage for valuables in public spaces",
            "Promote community awareness programs on theft prevention",
            "Increase visibility of security personnel during peak hours",
            "Use anti-theft locks and security tags on merchandise"
        ));
        MEASURES_BY_TYPE.put("Burglary", List.of(
            "Install burglar alarms and motion-sensor lighting in residential areas",
            "Encourage neighbourhood watch programs",
            "Strengthen door and window locks; use security grilles",
            "Coordinate with local community groups for vacant-property checks",
            "Improve perimeter lighting around residential buildings",
            "Promote use of smart home security systems"
        ));
        MEASURES_BY_TYPE.put("Robbery", List.of(
            "Improve street lighting in robbery-prone corridors",
            "Increase general security presence during evening and night hours",
            "Encourage pedestrians to stay in well-lit and crowded areas",
            "Install emergency call points in high-risk zones",
            "Conduct safety awareness workshops for residents",
            "Improve ATM security and camera coverage"
        ));
        MEASURES_BY_TYPE.put("Vehicle Theft", List.of(
            "Improve parking area surveillance with CCTV systems",
            "Use secure, designated parking facilities with access control",
            "Promote vehicle anti-theft systems (immobilisers, steering locks)",
            "Improve lighting around parking lots and residential streets",
            "Encourage use of GPS tracking devices on vehicles",
            "Conduct vehicle security awareness campaigns in the area"
        ));
        MEASURES_BY_TYPE.put("Cyber Crime", List.of(
            "Enable multi-factor authentication on all online accounts",
            "Avoid clicking on unverified or suspicious links",
            "Use strong, unique passwords and a password manager",
            "Conduct cybersecurity awareness campaigns in educational institutions and offices",
            "Regularly update software and operating systems",
            "Report phishing attempts to cybercrime helpline (1930)"
        ));
        MEASURES_BY_TYPE.put("Fraud", List.of(
            "Verify identities before making financial transactions",
            "Be cautious of unsolicited investment or job offers",
            "Check credentials of real-estate agents and service providers",
            "Report suspicious financial solicitations immediately",
            "Use official channels for all government-related services",
            "Educate community members about common fraud schemes"
        ));
        MEASURES_BY_TYPE.put("Assault", List.of(
            "Improve lighting and visibility in public areas and parks",
            "Increase general security presence in high-footfall public zones",
            "Promote conflict resolution and de-escalation awareness",
            "Encourage safe public-space practices",
            "Establish clear emergency contact points in public areas",
            "Promote bystander awareness and intervention training"
        ));
        MEASURES_BY_TYPE.put("Vandalism", List.of(
            "Install CCTV cameras in areas prone to vandalism",
            "Improve lighting around public infrastructure and parks",
            "Establish community ownership programs for public spaces",
            "Promptly repair damaged property to deter further vandalism",
            "Engage youth communities in constructive public art initiatives",
            "Coordinate with local civic bodies for regular area upkeep"
        ));
        MEASURES_BY_TYPE.put("Chain Snatching", List.of(
            "Avoid displaying jewellery conspicuously in public",
            "Use secure clasps and under-clothing methods for jewellery",
            "Improve lighting on commonly used pedestrian routes",
            "Be alert on two-wheeler-accessible roads especially near temples and markets",
            "Conduct awareness drives in vulnerable areas",
            "Report incidents immediately; share vehicle descriptions"
        ));
        MEASURES_BY_TYPE.put("Harassment", List.of(
            "Improve lighting and visibility around public transport stops",
            "Install emergency helpline call points in isolated public areas",
            "Promote awareness of harassment reporting channels",
            "Encourage bystander intervention awareness",
            "Conduct awareness programs in workplaces and educational institutions",
            "Improve general security presence during late-evening hours"
        ));

        // General advisories
        ADVISORY_BY_TYPE.put("Theft",
            "Petty theft tends to peak during busy market hours and evening crowding. " +
            "Awareness and surveillance improvements are the most effective preventive measures.");
        ADVISORY_BY_TYPE.put("Burglary",
            "Residential burglaries often occur during daytime when occupants are absent. " +
            "Community vigilance and physical security improvements are key deterrents.");
        ADVISORY_BY_TYPE.put("Robbery",
            "Robberies are more likely in poorly lit areas during late hours. " +
            "Infrastructure improvements and community awareness significantly reduce risk.");
        ADVISORY_BY_TYPE.put("Vehicle Theft",
            "Vehicle thefts are concentrated in areas with inadequate parking security. " +
            "Anti-theft technology combined with improved surveillance is highly effective.");
        ADVISORY_BY_TYPE.put("Cyber Crime",
            "Cyber crimes do not have a physical location but awareness campaigns " +
            "targeted at the general public in this area are strongly recommended.");
        ADVISORY_BY_TYPE.put("Fraud",
            "Financial fraud often targets vulnerable populations. Community education " +
            "and verification practices are the primary preventive tools.");
        ADVISORY_BY_TYPE.put("Assault",
            "Physical altercations often escalate in poorly monitored public spaces. " +
            "Infrastructure improvements and community programs are effective.");
        ADVISORY_BY_TYPE.put("Vandalism",
            "Vandalism increases in areas with poor lighting and low community ownership. " +
            "Civic engagement and timely maintenance are key prevention strategies.");
        ADVISORY_BY_TYPE.put("Chain Snatching",
            "Chain snatching is highly concentrated near markets, temples, and busy roads. " +
            "Awareness and lighting improvements are the most practical preventive measures.");
        ADVISORY_BY_TYPE.put("Harassment",
            "Harassment incidents tend to occur in isolated or poorly lit areas. " +
            "Infrastructure improvements and awareness programs are critical.");
    }

    @Autowired
    private CrimeRepository crimeRepository;

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Generates a recommendation for a given area and crime type.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Query DB for area-crime filtered records — O(n)</li>
     *   <li>Compute peak hour using TreeMap aggregation — O(n)</li>
     *   <li>Fetch preventive measures from HashMap — O(1)</li>
     *   <li>Compute hotspot score using AnalyticsService — O(n)</li>
     * </ol>
     *
     * @param area      Chennai area name
     * @param crimeType crime type name
     * @return populated {@link Recommendation} object
     */
    public Recommendation getRecommendation(String area, String crimeType) {
        List<CrimeRecord> allAreaRecords =
            crimeRepository.findByAreaIgnoreCase(area);

        // Filter by crime type — O(n) stream filter
        List<CrimeRecord> filtered = allAreaRecords.stream()
            .filter(r -> r.getCrimeType().equalsIgnoreCase(crimeType))
            .collect(Collectors.toCollection(ArrayList::new));

        long totalIncidents = filtered.size();

        // Peak hour — TreeMap<Integer, Long> auto-sorted
        TreeMap<Integer, Long> hourMap = new TreeMap<>();
        for (CrimeRecord r : filtered) {
            hourMap.merge(r.getHour(), 1L, Long::sum);
        }
        int peakHour = hourMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(18);

        String peakTimeWindow = String.format("%02d:00 – %02d:00", peakHour, (peakHour + 1) % 24);

        // Dominant severity — HashMap<String, Long>
        Map<String, Long> sevMap = new HashMap<>();
        for (CrimeRecord r : filtered) sevMap.merge(r.getSeverity(), 1L, Long::sum);
        String dominantSeverity = sevMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("MEDIUM");

        // Hotspot score from AnalyticsService
        AreaAnalysis areaAnalysis = analyticsService.analyzeArea(area);
        double score  = areaAnalysis != null ? areaAnalysis.getHotspotScore() : 0.3;
        String level  = analyticsService.classifyHotspot(score);

        // Pattern summary
        String patternSummary = buildPatternSummary(area, crimeType, totalIncidents,
                                                     peakTimeWindow, dominantSeverity);

        // Preventive measures — HashMap O(1) lookup
        List<String> measures = MEASURES_BY_TYPE.getOrDefault(crimeType,
            List.of("Improve general security awareness in the area",
                    "Coordinate with local authorities for targeted prevention",
                    "Strengthen community watch programs"));

        String advisory = ADVISORY_BY_TYPE.getOrDefault(crimeType,
            "General security awareness and infrastructure improvements are recommended.");

        return Recommendation.builder()
            .area(area)
            .crimeType(crimeType)
            .patternSummary(patternSummary)
            .peakTimeWindow(peakTimeWindow)
            .dominantSeverity(dominantSeverity)
            .hotspotLevel(level)
            .hotspotScore(score)
            .totalIncidents(totalIncidents)
            .preventiveMeasures(new ArrayList<>(measures))
            .generalAdvisory(advisory)
            .build();
    }

    /**
     * Returns recommendations for all crime types for a given area.
     * Uses an ArrayList to collect results — O(n * t) where t = crime types.
     */
    public List<Recommendation> getRecommendationsForArea(String area) {
        List<String> crimeTypes = List.of(
            "Theft", "Burglary", "Robbery", "Vehicle Theft", "Cyber Crime",
            "Fraud", "Assault", "Vandalism", "Chain Snatching", "Harassment"
        );

        List<Recommendation> results = new ArrayList<>();
        for (String type : crimeTypes) {
            results.add(getRecommendation(area, type));
        }

        // Sort by total incidents descending — O(t log t)
        results.sort(Comparator.comparingLong(Recommendation::getTotalIncidents).reversed());
        return results;
    }

    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------

    private String buildPatternSummary(String area, String crimeType,
                                        long count, String peakTime, String severity) {
        if (count == 0) {
            return String.format(
                "No synthetic records found for %s in %s. " +
                "General prevention measures still apply.", crimeType, area);
        }
        return String.format(
            "The synthetic dataset contains %d %s records for %s. " +
            "The pattern shows higher activity around %s with %s severity being most common. " +
            "Note: This analysis is based on generated academic data only.",
            count, crimeType, area, peakTime, severity.toLowerCase());
    }
}
