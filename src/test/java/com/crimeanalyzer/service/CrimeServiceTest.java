package com.crimeanalyzer.service;

import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.repository.CrimeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CrimeService}.
 *
 * <p>Tests demonstrate:
 * <ul>
 *   <li>Valid and invalid input handling</li>
 *   <li>Service-layer business logic</li>
 *   <li>Mocking of the repository layer</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrimeService Tests")
class CrimeServiceTest {

    @Mock
    private CrimeRepository crimeRepository;

    @InjectMocks
    private CrimeService crimeService;

    private CrimeRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = CrimeRecord.builder()
            .id(1L)
            .crimeId("CMA-000001")
            .crimeDate(LocalDate.of(2023, 5, 15))
            .crimeTime(LocalTime.of(18, 30))
            .year(2023)
            .month(5)
            .dayOfWeek("Monday")
            .hour(18)
            .area("T Nagar")
            .zone("Central")
            .crimeType("Theft")
            .severity("MEDIUM")
            .latitude(13.0418)
            .longitude(80.2341)
            .description("Petty theft near market")
            .caseStatus("REPORTED")
            .build();
    }

    // -------------------------------------------------------
    // findById
    // -------------------------------------------------------

    @Test
    @DisplayName("findById returns record when exists")
    void findById_returnsRecord_whenExists() {
        when(crimeRepository.findById(1L)).thenReturn(Optional.of(sampleRecord));

        Optional<CrimeRecord> result = crimeService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getArea()).isEqualTo("T Nagar");
    }

    @Test
    @DisplayName("findById returns empty when not found")
    void findById_returnsEmpty_whenNotFound() {
        when(crimeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CrimeRecord> result = crimeService.findById(999L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------
    // findByArea validation
    // -------------------------------------------------------

    @Test
    @DisplayName("findByArea throws when area is blank")
    void findByArea_throwsIllegalArgument_whenBlank() {
        assertThatThrownBy(() -> crimeService.findByArea(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("area");
    }

    @Test
    @DisplayName("findByArea throws when area is null")
    void findByArea_throwsIllegalArgument_whenNull() {
        assertThatThrownBy(() -> crimeService.findByArea(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("findByArea returns list for valid area")
    void findByArea_returnsList_forValidArea() {
        when(crimeRepository.findByAreaIgnoreCase("T Nagar"))
            .thenReturn(List.of(sampleRecord));

        List<CrimeRecord> result = crimeService.findByArea("T Nagar");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCrimeType()).isEqualTo("Theft");
    }

    // -------------------------------------------------------
    // findBySeverity validation
    // -------------------------------------------------------

    @Test
    @DisplayName("findBySeverity throws for invalid severity")
    void findBySeverity_throwsIllegalArgument_forInvalid() {
        assertThatThrownBy(() -> crimeService.findBySeverity("EXTREME"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("severity");
    }

    @Test
    @DisplayName("findBySeverity accepts valid values (case-insensitive)")
    void findBySeverity_acceptsValidValues() {
        when(crimeRepository.findBySeverity("HIGH")).thenReturn(List.of());

        assertThatCode(() -> crimeService.findBySeverity("high"))
            .doesNotThrowAnyException();

        verify(crimeRepository).findBySeverity("HIGH");
    }

    // -------------------------------------------------------
    // create validation
    // -------------------------------------------------------

    @Test
    @DisplayName("create throws when required fields are missing")
    void create_throws_whenRequiredFieldsMissing() {
        CrimeRecord invalid = CrimeRecord.builder()
            .crimeDate(LocalDate.now())
            .crimeType("Theft")
            // area is missing
            .build();

        assertThatThrownBy(() -> crimeService.create(invalid))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create saves valid record")
    void create_savesRecord_whenValid() {
        CrimeRecord input = CrimeRecord.builder()
            .crimeDate(LocalDate.now())
            .area("Anna Nagar")
            .crimeType("Theft")
            .severity("LOW")
            .build();
        when(crimeRepository.save(any())).thenReturn(sampleRecord);

        CrimeRecord saved = crimeService.create(input);

        assertThat(saved).isNotNull();
        verify(crimeRepository).save(any());
    }

    // -------------------------------------------------------
    // delete
    // -------------------------------------------------------

    @Test
    @DisplayName("delete throws NoSuchElementException for non-existent ID")
    void delete_throws_whenNotFound() {
        when(crimeRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> crimeService.delete(999L))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("delete succeeds when record exists")
    void delete_succeeds_whenRecordExists() {
        when(crimeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(crimeRepository).deleteById(1L);

        assertThatCode(() -> crimeService.delete(1L)).doesNotThrowAnyException();
        verify(crimeRepository).deleteById(1L);
    }

    // -------------------------------------------------------
    // filter
    // -------------------------------------------------------

    @Test
    @DisplayName("findByFilters returns empty list for no matches")
    void findByFilters_returnsEmpty_forNoMatches() {
        when(crimeRepository.findByFilters(any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        List<CrimeRecord> result = crimeService.findByFilters(
            "Nonexistent Area", null, null, null, null);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------
    // Transient helper methods
    // -------------------------------------------------------

    @Test
    @DisplayName("isNightCrime returns true for hour >= 20")
    void isNightCrime_returnsTrue_forLateHour() {
        sampleRecord.setHour(22);
        // Use the transient method directly
        CrimeRecord r = CrimeRecord.builder().hour(22)
            .crimeDate(LocalDate.now()).area("Test").crimeType("Theft").build();
        assertThat(r.isNightCrime()).isTrue();
    }

    @Test
    @DisplayName("isPeakEveningCrime returns true for evening hours")
    void isPeakEveningCrime_returnsTrue_forEveningHour() {
        CrimeRecord r = CrimeRecord.builder().hour(19)
            .crimeDate(LocalDate.now()).area("Test").crimeType("Theft").build();
        assertThat(r.isPeakEveningCrime()).isTrue();
    }
}
