package com.crimeanalyzer.service;

import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.repository.CrimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Business logic service for CRUD operations on {@link CrimeRecord}.
 *
 * <p>Demonstrates OOP service-layer abstraction with Spring dependency injection.
 * Uses the Java Collections Framework for in-memory operations.
 *
 * <p>Method complexities:
 * <ul>
 *   <li>{@code findById}: O(1) (primary-key lookup)</li>
 *   <li>{@code findByFilters}: O(n) (filtered scan with DB indexes)</li>
 *   <li>{@code getDistinctAreas}: O(n) DISTINCT scan, then sorted O(k log k)</li>
 * </ul>
 */
@Service
public class CrimeService {

    private static final Logger log = LoggerFactory.getLogger(CrimeService.class);

    private static final Set<String> VALID_SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH");
    private static final Set<String> VALID_STATUSES =
        Set.of("REPORTED", "UNDER_INVESTIGATION", "CLOSED");

    @Autowired
    private CrimeRepository crimeRepository;

    // -------------------------------------------------------
    // Read operations
    // -------------------------------------------------------

    public Optional<CrimeRecord> findById(Long id) {
        return crimeRepository.findById(id);
    }

    public Optional<CrimeRecord> findByCrimeId(String crimeId) {
        return crimeRepository.findByCrimeId(crimeId);
    }

    /**
     * Returns all crime records (use with caution on large datasets).
     * Complexity: O(n)
     */
    public List<CrimeRecord> findAll() {
        return crimeRepository.findAll();
    }

    /**
     * Returns a paginated list of all records sorted by date descending.
     * Complexity: O(n) for DB scan, O(1) for page extraction.
     */
    public Page<CrimeRecord> findAllPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "crimeDate"));
        return crimeRepository.findAll(pageable);
    }

    public List<CrimeRecord> findByArea(String area) {
        validateNotBlank(area, "area");
        return crimeRepository.findByAreaIgnoreCase(area);
    }

    public List<CrimeRecord> findByCrimeType(String crimeType) {
        validateNotBlank(crimeType, "crimeType");
        return crimeRepository.findByCrimeTypeIgnoreCase(crimeType);
    }

    public List<CrimeRecord> findBySeverity(String severity) {
        validateSeverity(severity);
        return crimeRepository.findBySeverity(severity.toUpperCase());
    }

    /**
     * Composite filter — any parameter may be null to skip that filter.
     * Complexity: O(n) filtered scan
     */
    public List<CrimeRecord> findByFilters(String area, String crimeType,
                                            String severity,
                                            LocalDate fromDate, LocalDate toDate) {
        String normArea      = blankToNull(area);
        String normCrimeType = blankToNull(crimeType);
        String normSeverity  = blankToNull(severity) != null
                               ? severity.toUpperCase() : null;

        return crimeRepository.findByFilters(normArea, normCrimeType, normSeverity, fromDate, toDate);
    }

    public Page<CrimeRecord> findByFiltersPaged(String area, String crimeType,
                                                 String severity,
                                                 LocalDate fromDate, LocalDate toDate,
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "crimeDate"));
        String normArea      = blankToNull(area);
        String normCrimeType = blankToNull(crimeType);
        String normSeverity  = blankToNull(severity) != null ? severity.toUpperCase() : null;

        return crimeRepository.findByFiltersPaged(normArea, normCrimeType, normSeverity,
                                                   fromDate, toDate, pageable);
    }

    // -------------------------------------------------------
    // Write operations
    // -------------------------------------------------------

    /**
     * Creates a new crime record after validation.
     */
    public CrimeRecord create(CrimeRecord record) {
        validateRecord(record);
        record.setId(null); // ensure new insert
        return crimeRepository.save(record);
    }

    /**
     * Updates an existing record by ID.
     *
     * @throws NoSuchElementException if record not found
     */
    public CrimeRecord update(Long id, CrimeRecord updated) {
        CrimeRecord existing = crimeRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Crime record not found: " + id));
        validateRecord(updated);
        updated.setId(existing.getId());
        updated.setCrimeId(existing.getCrimeId());
        return crimeRepository.save(updated);
    }

    /**
     * Deletes a record by ID.
     *
     * @throws NoSuchElementException if record not found
     */
    public void delete(Long id) {
        if (!crimeRepository.existsById(id)) {
            throw new NoSuchElementException("Crime record not found: " + id);
        }
        crimeRepository.deleteById(id);
    }

    // -------------------------------------------------------
    // Counts
    // -------------------------------------------------------

    public long getTotalCount() { return crimeRepository.count(); }

    public long getHighSeverityCount() { return crimeRepository.countBySeverity("HIGH"); }

    // -------------------------------------------------------
    // Distinct value lists for dropdowns
    // Uses HashSet internally to deduplicate, then sorted ArrayList
    // Complexity: O(n) + O(k log k) sort
    // -------------------------------------------------------

    public List<String> getDistinctAreas() {
        return crimeRepository.findDistinctAreas();
    }

    public List<String> getDistinctCrimeTypes() {
        return crimeRepository.findDistinctCrimeTypes();
    }

    public List<String> getDistinctZones() {
        return crimeRepository.findDistinctZones();
    }

    // -------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------

    private void validateRecord(CrimeRecord record) {
        Objects.requireNonNull(record, "Crime record must not be null");
        validateNotBlank(record.getArea(), "area");
        validateNotBlank(record.getCrimeType(), "crimeType");
        if (record.getSeverity() != null) validateSeverity(record.getSeverity());
        if (record.getCrimeDate() == null) throw new IllegalArgumentException("Date is required");
    }

    private void validateSeverity(String severity) {
        if (!VALID_SEVERITIES.contains(severity.toUpperCase())) {
            throw new IllegalArgumentException(
                "Invalid severity: '" + severity + "'. Must be one of " + VALID_SEVERITIES);
        }
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must not be blank");
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
