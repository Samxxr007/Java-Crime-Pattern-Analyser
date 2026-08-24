package com.crimeanalyzer.service;

import com.crimeanalyzer.model.AreaAnalysis;
import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.model.Recommendation;
import com.crimeanalyzer.repository.CrimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RecommendationService}.
 * Tests recommendation generation, measure mapping, and advisory output.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService Tests")
class RecommendationServiceTest {

    @Mock
    private CrimeRepository crimeRepository;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private RecommendationService recommendationService;

    private List<CrimeRecord> mockRecords;

    @BeforeEach
    void setUp() {
        mockRecords = List.of(
                CrimeRecord.builder()
                        .id(1L)
                        .crimeId("CMA-000001")
                        .crimeDate(LocalDate.of(2023, 8, 1))
                        .crimeTime(LocalTime.of(19, 0))
                        .year(2023)
                        .month(8)
                        .dayOfWeek("Tuesday")
                        .hour(19)
                        .area("T Nagar")
                        .zone("Central")
                        .crimeType("Vehicle Theft")
                        .severity("MEDIUM")
                        .caseStatus("REPORTED")
                        .build(),
                CrimeRecord.builder()
                        .id(2L)
                        .crimeId("CMA-000002")
                        .crimeDate(LocalDate.of(2023, 8, 2))
                        .crimeTime(LocalTime.of(19, 30))
                        .year(2023)
                        .month(8)
                        .dayOfWeek("Wednesday")
                        .hour(19)
                        .area("T Nagar")
                        .zone("Central")
                        .crimeType("Vehicle Theft")
                        .severity("HIGH")
                        .caseStatus("UNDER_INVESTIGATION")
                        .build()
        );
    }

    @Test
    @DisplayName("getRecommendation returns valid prevention measures and summary")
    void getRecommendation_returnsPopulatedRecommendation() {
        when(crimeRepository.findByAreaIgnoreCase("T Nagar")).thenReturn(mockRecords);

        AreaAnalysis mockAnalysis = AreaAnalysis.builder()
                .areaName("T Nagar")
                .hotspotScore(0.65)
                .build();
        when(analyticsService.analyzeArea("T Nagar")).thenReturn(mockAnalysis);
        when(analyticsService.classifyHotspot(0.65)).thenReturn("HIGH");

        Recommendation rec = recommendationService.getRecommendation("T Nagar", "Vehicle Theft");

        assertThat(rec).isNotNull();
        assertThat(rec.getArea()).isEqualTo("T Nagar");
        assertThat(rec.getCrimeType()).isEqualTo("Vehicle Theft");
        assertThat(rec.getTotalIncidents()).isEqualTo(2);
        assertThat(rec.getHotspotLevel()).isEqualTo("HIGH");
        assertThat(rec.getPeakTimeWindow()).isEqualTo("19:00 – 20:00");
        assertThat(rec.getPreventiveMeasures()).isNotEmpty();
        assertThat(rec.getPreventiveMeasures()).anyMatch(m -> m.toLowerCase().contains("parking") || m.toLowerCase().contains("cctv") || m.toLowerCase().contains("anti-theft"));
        assertThat(rec.getGeneralAdvisory()).isNotBlank();
    }

    @Test
    @DisplayName("getRecommendationsForArea returns recommendations across all categories")
    void getRecommendationsForArea_returnsAllCategories() {
        when(crimeRepository.findByAreaIgnoreCase("T Nagar")).thenReturn(mockRecords);
        when(analyticsService.analyzeArea(anyString())).thenReturn(
                AreaAnalysis.builder().areaName("T Nagar").hotspotScore(0.40).build()
        );
        when(analyticsService.classifyHotspot(0.40)).thenReturn("MODERATE");

        List<Recommendation> allRecs = recommendationService.getRecommendationsForArea("T Nagar");

        assertThat(allRecs).isNotEmpty();
        assertThat(allRecs.size()).isGreaterThanOrEqualTo(10);
        assertThat(allRecs.get(0).getTotalIncidents()).isGreaterThanOrEqualTo(allRecs.get(allRecs.size() - 1).getTotalIncidents());
    }
}
