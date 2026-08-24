package com.crimeanalyzer.repository;

import com.crimeanalyzer.model.CrimeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link CrimeRecord}.
 *
 * <p>Provides CRUD operations plus custom JPQL queries for
 * filtering, counting, and distribution calculations.
 *
 * <p>Complexity notes:
 * <ul>
 *   <li>All count/group-by queries: O(n) table scan + aggregation</li>
 *   <li>findById: O(1) via primary-key index</li>
 *   <li>findByArea, findByCrimeType: O(log n) via B-tree indexes</li>
 * </ul>
 */
@Repository
public interface CrimeRepository extends JpaRepository<CrimeRecord, Long> {

    // -------------------------------------------------------
    // Basic finders
    // -------------------------------------------------------

    Optional<CrimeRecord> findByCrimeId(String crimeId);

    List<CrimeRecord> findByArea(String area);

    List<CrimeRecord> findByAreaIgnoreCase(String area);

    List<CrimeRecord> findByCrimeType(String crimeType);

    List<CrimeRecord> findByCrimeTypeIgnoreCase(String crimeType);

    List<CrimeRecord> findBySeverity(String severity);

    List<CrimeRecord> findByCaseStatus(String caseStatus);

    // -------------------------------------------------------
    // Pageable finders (for records table)
    // -------------------------------------------------------

    Page<CrimeRecord> findByAreaIgnoreCase(String area, Pageable pageable);

    Page<CrimeRecord> findByCrimeTypeIgnoreCase(String crimeType, Pageable pageable);

    Page<CrimeRecord> findBySeverityIgnoreCase(String severity, Pageable pageable);

    // -------------------------------------------------------
    // Date range
    // -------------------------------------------------------

    List<CrimeRecord> findByCrimeDateBetween(LocalDate from, LocalDate to);

    // -------------------------------------------------------
    // Composite filters
    // -------------------------------------------------------

    @Query("""
        SELECT c FROM CrimeRecord c
        WHERE (:area IS NULL OR LOWER(c.area) = LOWER(:area))
          AND (:crimeType IS NULL OR LOWER(c.crimeType) = LOWER(:crimeType))
          AND (:severity IS NULL OR LOWER(c.severity) = LOWER(:severity))
          AND (:fromDate IS NULL OR c.crimeDate >= :fromDate)
          AND (:toDate IS NULL OR c.crimeDate <= :toDate)
        ORDER BY c.crimeDate DESC
        """)
    List<CrimeRecord> findByFilters(
        @Param("area")      String area,
        @Param("crimeType") String crimeType,
        @Param("severity")  String severity,
        @Param("fromDate")  LocalDate fromDate,
        @Param("toDate")    LocalDate toDate
    );

    @Query("""
        SELECT c FROM CrimeRecord c
        WHERE (:area IS NULL OR LOWER(c.area) = LOWER(:area))
          AND (:crimeType IS NULL OR LOWER(c.crimeType) = LOWER(:crimeType))
          AND (:severity IS NULL OR LOWER(c.severity) = LOWER(:severity))
          AND (:fromDate IS NULL OR c.crimeDate >= :fromDate)
          AND (:toDate IS NULL OR c.crimeDate <= :toDate)
        ORDER BY c.crimeDate DESC
        """)
    Page<CrimeRecord> findByFiltersPaged(
        @Param("area")      String area,
        @Param("crimeType") String crimeType,
        @Param("severity")  String severity,
        @Param("fromDate")  LocalDate fromDate,
        @Param("toDate")    LocalDate toDate,
        Pageable pageable
    );

    // -------------------------------------------------------
    // Aggregation queries for analytics
    // -------------------------------------------------------

    /** Returns [area, count] pairs ordered by count descending. */
    @Query("SELECT c.area, COUNT(c) FROM CrimeRecord c GROUP BY c.area ORDER BY COUNT(c) DESC")
    List<Object[]> countByArea();

    /** Returns [crimeType, count] pairs. */
    @Query("SELECT c.crimeType, COUNT(c) FROM CrimeRecord c GROUP BY c.crimeType ORDER BY COUNT(c) DESC")
    List<Object[]> countByCrimeType();

    /** Returns [month, count] pairs. */
    @Query("SELECT c.month, COUNT(c) FROM CrimeRecord c GROUP BY c.month ORDER BY c.month ASC")
    List<Object[]> countByMonth();

    /** Returns [hour, count] pairs. */
    @Query("SELECT c.hour, COUNT(c) FROM CrimeRecord c GROUP BY c.hour ORDER BY c.hour ASC")
    List<Object[]> countByHour();

    /** Returns [dayOfWeek, count] pairs. */
    @Query("SELECT c.dayOfWeek, COUNT(c) FROM CrimeRecord c GROUP BY c.dayOfWeek ORDER BY COUNT(c) DESC")
    List<Object[]> countByDayOfWeek();

    /** Returns [year, count] pairs. */
    @Query("SELECT c.year, COUNT(c) FROM CrimeRecord c GROUP BY c.year ORDER BY c.year ASC")
    List<Object[]> countByYear();

    /** Returns [severity, count] pairs. */
    @Query("SELECT c.severity, COUNT(c) FROM CrimeRecord c GROUP BY c.severity ORDER BY COUNT(c) DESC")
    List<Object[]> countBySeverity();

    /** Returns [caseStatus, count] pairs. */
    @Query("SELECT c.caseStatus, COUNT(c) FROM CrimeRecord c GROUP BY c.caseStatus")
    List<Object[]> countByStatus();

    // -------------------------------------------------------
    // Hotspot calculations
    // -------------------------------------------------------

    @Query("SELECT COUNT(c) FROM CrimeRecord c WHERE c.severity = 'HIGH' AND c.area = :area")
    long countHighSeverityByArea(@Param("area") String area);

    @Query("SELECT COUNT(c) FROM CrimeRecord c WHERE c.area = :area AND c.year = :year")
    long countByAreaAndYear(@Param("area") String area, @Param("year") int year);

    // -------------------------------------------------------
    // Distinct value lists (for filter dropdowns)
    // -------------------------------------------------------

    @Query("SELECT DISTINCT c.area FROM CrimeRecord c ORDER BY c.area ASC")
    List<String> findDistinctAreas();

    @Query("SELECT DISTINCT c.crimeType FROM CrimeRecord c ORDER BY c.crimeType ASC")
    List<String> findDistinctCrimeTypes();

    @Query("SELECT DISTINCT c.zone FROM CrimeRecord c ORDER BY c.zone ASC")
    List<String> findDistinctZones();

    // -------------------------------------------------------
    // Counts
    // -------------------------------------------------------

    long countBySeverity(String severity);

    long countByArea(String area);

    boolean existsByCrimeId(String crimeId);
}
