package com.crimeanalyzer.service;

import com.crimeanalyzer.model.AreaAnalysis;
import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.model.CrimeStatistics;
import com.crimeanalyzer.model.HotspotResult;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AnalyticsService}.
 * Validates analytical aggregation logic, collections handling, and hotspot scoring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Tests")
class AnalyticsServiceTest {

    @Mock
    private CrimeRepository crimeRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private List<CrimeRecord> mockRecords;

    @BeforeEach
    void setUp() {
        mockRecords = new ArrayList<>();

        mockRecords.add(CrimeRecord.builder()
                .id(1L)
                .crimeId("CMA-000001")
                .crimeDate(LocalDate.of(2023, 5, 10))
                .crimeTime(LocalTime.of(19, 30))
                .year(2023)
                .month(5)
                .dayOfWeek("Wednesday")
                .hour(19)
                .area("Anna Nagar")
                .zone("North")
                .crimeType("Theft")
                .severity("MEDIUM")
                .latitude(13.0850)
                .longitude(80.2101)
                .caseStatus("REPORTED")
                .build());

        mockRecords.add(CrimeRecord.builder()
                .id(2L)
                .crimeId("CMA-000002")
                .crimeDate(LocalDate.of(2023, 5, 12))
                .crimeTime(LocalTime.of(19, 45))
                .year(2023)
                .month(5)
                .dayOfWeek("Friday")
                .hour(19)
                .area("Anna Nagar")
                .zone("North")
                .crimeType("Theft")
                .severity("HIGH")
                .latitude(13.0851)
                .longitude(80.2102)
                .caseStatus("CLOSED")
                .build());

        mockRecords.add(CrimeRecord.builder()
                .id(3L)
                .crimeId("CMA-000003")
                .crimeDate(LocalDate.of(2023, 6, 15))
                .crimeTime(LocalTime.of(14, 0))
                .year(2023)
                .month(6)
                .dayOfWeek("Thursday")
                .hour(14)
                .area("Anna Nagar")
                .zone("North")
                .crimeType("Cyber Crime")
                .severity("LOW")
                .latitude(13.0849)
                .longitude(80.2100)
                .caseStatus("UNDER_INVESTIGATION")
                .build());
    }

    @Test
    @DisplayName("getOverallStatistics returns populated CrimeStatistics")
    void getOverallStatistics_returnsPopulatedStats() {
        List<Object[]> areaCounts = List.of(new Object[]{"Anna Nagar", 3L}, new Object[]{"T Nagar", 2L});
        List<Object[]> typeCounts = List.of(new Object[]{"Theft", 2L}, new Object[]{"Cyber Crime", 1L});
        List<Object[]> monthCounts = List.of(new Object[]{5, 2L}, new Object[]{6, 1L});
        List<Object[]> hourCounts = List.of(new Object[]{14, 1L}, new Object[]{19, 2L});
        List<Object[]> dayCounts = List.of(new Object[]{"Friday", 1L}, new Object[]{"Wednesday", 1L});
        List<Object[]> yearCounts = List.of(new Object[]{2023, 3L});
        List<Object[]> sevCounts = List.of(new Object[]{"HIGH", 1L}, new Object[]{"MEDIUM", 1L}, new Object[]{"LOW", 1L});
        List<Object[]> statusCounts = List.of(new Object[]{"REPORTED", 1L}, new Object[]{"CLOSED", 1L}, new Object[]{"UNDER_INVESTIGATION", 1L});

        when(crimeRepository.countByArea()).thenReturn(areaCounts);
        when(crimeRepository.countByCrimeType()).thenReturn(typeCounts);
        when(crimeRepository.countByMonth()).thenReturn(monthCounts);
        when(crimeRepository.countByHour()).thenReturn(hourCounts);
        when(crimeRepository.countByDayOfWeek()).thenReturn(dayCounts);
        when(crimeRepository.countByYear()).thenReturn(yearCounts);
        when(crimeRepository.countBySeverity()).thenReturn(sevCounts);
        when(crimeRepository.countByStatus()).thenReturn(statusCounts);
        when(crimeRepository.count()).thenReturn(3L);

        CrimeStatistics stats = analyticsService.getOverallStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalRecords()).isEqualTo(3L);
        assertThat(stats.getTotalAreas()).isEqualTo(2);
        assertThat(stats.getMostFrequentCrimeType()).isEqualTo("Theft");
        assertThat(stats.getMostActiveArea()).isEqualTo("Anna Nagar");
        assertThat(stats.getHighSeverityCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("analyzeArea returns accurate per-area analytics")
    void analyzeArea_returnsValidAnalysis() {
        when(crimeRepository.findByAreaIgnoreCase("Anna Nagar")).thenReturn(mockRecords);
        when(crimeRepository.count()).thenReturn(3L);

        AreaAnalysis analysis = analyticsService.analyzeArea("Anna Nagar");

        assertThat(analysis).isNotNull();
        assertThat(analysis.getAreaName()).isEqualTo("Anna Nagar");
        assertThat(analysis.getTotalRecords()).isEqualTo(3);
        assertThat(analysis.getMostCommonCrimeType()).isEqualTo("Theft");
        assertThat(analysis.getPeakHour()).isEqualTo(19);
        assertThat(analysis.getPeakHourRange()).isEqualTo("19:00 – 20:00");
        assertThat(analysis.getMonthlyTrend()).containsEntry(5, 2L);
        assertThat(analysis.getMonthlyTrend()).containsEntry(6, 1L);
    }

    @Test
    @DisplayName("analyzeArea returns null when area has no records")
    void analyzeArea_returnsNull_whenNoRecords() {
        when(crimeRepository.findByAreaIgnoreCase("Unknown Area")).thenReturn(Collections.emptyList());

        AreaAnalysis analysis = analyticsService.analyzeArea("Unknown Area");

        assertThat(analysis).isNull();
    }

    @Test
    @DisplayName("computeHotspots calculates hotspot ranking list")
    void computeHotspots_calculatesAndRanks() {
        List<Object[]> areaCounts = List.of(new Object[]{"Anna Nagar", 100L}, new Object[]{"T Nagar", 50L});
        List<Object[]> sevCounts = List.of(new Object[]{"HIGH", 30L}, new Object[]{"MEDIUM", 40L});
        List<Object[]> typeCounts = List.of(new Object[]{"Theft", 60L});

        when(crimeRepository.countByArea()).thenReturn(areaCounts);
        when(crimeRepository.countBySeverity()).thenReturn(sevCounts);
        when(crimeRepository.countByCrimeType()).thenReturn(typeCounts);
        when(crimeRepository.countHighSeverityByArea(anyString())).thenReturn(10L);
        when(crimeRepository.countByAreaAndYear(anyString(), anyInt())).thenReturn(5L);

        List<HotspotResult> hotspots = analyticsService.computeHotspots();

        assertThat(hotspots).isNotEmpty();
        assertThat(hotspots).hasSize(2);
        assertThat(hotspots.get(0).getHotspotScore()).isGreaterThanOrEqualTo(hotspots.get(1).getHotspotScore());
    }

    @Test
    @DisplayName("classifyHotspot assigns appropriate severity levels")
    void classifyHotspot_assignsCorrectLevels() {
        assertThat(analyticsService.classifyHotspot(0.75)).isEqualTo("HIGH");
        assertThat(analyticsService.classifyHotspot(0.45)).isEqualTo("MODERATE");
        assertThat(analyticsService.classifyHotspot(0.20)).isEqualTo("LOW");
    }
}
