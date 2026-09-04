package com.crimeanalyzer.service;

import com.crimeanalyzer.model.AreaAnalysis;
import com.crimeanalyzer.model.AreaComparison;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private ComparisonService comparisonService;

    @Test
    void compareAreas_returnsValidComparison_whenBothAreasExist() {
        AreaAnalysis an1 = AreaAnalysis.builder()
                .areaName("Anna Nagar")
                .totalRecords(100)
                .mostCommonCrimeType("Theft")
                .peakHourRange("18:00 - 22:00")
                .hotspotLevel("HIGH")
                .hotspotScore(0.65)
                .crimeTypeDistribution(Map.of("Theft", 40L, "Burglary", 20L))
                .severityDistribution(Map.of("HIGH", 30L, "MEDIUM", 40L, "LOW", 30L))
                .build();

        AreaAnalysis an2 = AreaAnalysis.builder()
                .areaName("T Nagar")
                .totalRecords(120)
                .mostCommonCrimeType("Vehicle Theft")
                .peakHourRange("17:00 - 21:00")
                .hotspotLevel("MODERATE")
                .hotspotScore(0.48)
                .crimeTypeDistribution(Map.of("Vehicle Theft", 50L, "Theft", 30L))
                .severityDistribution(Map.of("HIGH", 15L, "MEDIUM", 50L, "LOW", 55L))
                .build();

        when(analyticsService.analyzeArea("Anna Nagar")).thenReturn(an1);
        when(analyticsService.analyzeArea("T Nagar")).thenReturn(an2);

        AreaComparison comp = comparisonService.compareAreas("Anna Nagar", "T Nagar");

        assertThat(comp).isNotNull();
        assertThat(comp.getArea1()).isEqualTo("Anna Nagar");
        assertThat(comp.getArea2()).isEqualTo("T Nagar");
        assertThat(comp.getTotalIncidentsArea1()).isEqualTo(100);
        assertThat(comp.getTotalIncidentsArea2()).isEqualTo(120);
        assertThat(comp.getSafetyScoreArea1()).isBetween(15, 95);
        assertThat(comp.getSafetyScoreArea2()).isBetween(15, 95);
        assertThat(comp.getCrimeSeverityIndexArea1()).isGreaterThan(0.0);
        assertThat(comp.getComparativeSummary()).contains("Anna Nagar");
    }

    @Test
    void compareAreas_throwsException_whenAreaNotFound() {
        when(analyticsService.analyzeArea("UnknownArea")).thenReturn(null);

        assertThatThrownBy(() -> comparisonService.compareAreas("UnknownArea", "T Nagar"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
