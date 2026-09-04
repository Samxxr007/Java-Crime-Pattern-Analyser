package com.crimeanalyzer.service;

import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.model.ProximityResult;
import com.crimeanalyzer.repository.CrimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProximityServiceTest {

    @Mock
    private CrimeRepository crimeRepository;

    @InjectMocks
    private ProximityService proximityService;

    private List<CrimeRecord> mockRecords;

    @BeforeEach
    void setUp() {
        // Anna Nagar: ~13.0850, 80.2101
        CrimeRecord r1 = CrimeRecord.builder()
                .id(1L)
                .crimeId("CMA-0001")
                .crimeDate(LocalDate.of(2024, 1, 10))
                .crimeTime(LocalTime.of(19, 30))
                .area("Anna Nagar")
                .zone("Central")
                .crimeType("Theft")
                .severity("HIGH")
                .latitude(13.0850)
                .longitude(80.2101)
                .caseStatus("UNDER_INVESTIGATION")
                .build();

        // Nearby Anna Nagar (~0.5 km away)
        CrimeRecord r2 = CrimeRecord.builder()
                .id(2L)
                .crimeId("CMA-0002")
                .crimeDate(LocalDate.of(2024, 1, 12))
                .crimeTime(LocalTime.of(20, 15))
                .area("Anna Nagar")
                .zone("Central")
                .crimeType("Vehicle Theft")
                .severity("LOW")
                .latitude(13.0870)
                .longitude(80.2120)
                .caseStatus("CLOSED")
                .build();

        // Far away: Tambaram (~20 km away: 12.9249, 80.1000)
        CrimeRecord r3 = CrimeRecord.builder()
                .id(3L)
                .crimeId("CMA-0003")
                .crimeDate(LocalDate.of(2024, 1, 15))
                .crimeTime(LocalTime.of(14, 0))
                .area("Tambaram")
                .zone("South")
                .crimeType("Burglary")
                .severity("MEDIUM")
                .latitude(12.9249)
                .longitude(80.1000)
                .caseStatus("CLOSED")
                .build();

        mockRecords = List.of(r1, r2, r3);
    }

    @Test
    void findIncidentsWithinRadius_returnsOnlyNearbyIncidents() {
        when(crimeRepository.findAll()).thenReturn(mockRecords);

        // Scan 2 km around Anna Nagar center
        ProximityResult result = proximityService.findIncidentsWithinRadius(13.0850, 80.2101, 2.0);

        assertThat(result).isNotNull();
        assertThat(result.getTotalIncidents()).isEqualTo(2);
        assertThat(result.getNearestIncidentArea()).isEqualTo("Anna Nagar");
        assertThat(result.getIncidents()).hasSize(2);
        assertThat(result.getIncidents().get(0).getDistanceKm()).isLessThanOrEqualTo(result.getIncidents().get(1).getDistanceKm());
    }

    @Test
    void findIncidentsWithinRadius_returnsEmpty_whenNoIncidentsInRadius() {
        when(crimeRepository.findAll()).thenReturn(mockRecords);

        // Point out at sea or far off
        ProximityResult result = proximityService.findIncidentsWithinRadius(14.0000, 81.0000, 1.0);

        assertThat(result).isNotNull();
        assertThat(result.getTotalIncidents()).isZero();
        assertThat(result.getIncidents()).isEmpty();
    }
}
