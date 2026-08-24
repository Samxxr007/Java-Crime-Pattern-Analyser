package com.crimeanalyzer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity representing a single synthetic crime record.
 *
 * <p>Demonstrates OOP principles:
 * <ul>
 *   <li><b>Encapsulation</b> — private fields with Lombok-generated getters/setters</li>
 *   <li><b>Builder pattern</b> — fluent construction via {@code @Builder}</li>
 * </ul>
 *
 * <p><strong>NOTE:</strong> All records in the database are synthetic and generated
 * programmatically for academic demonstration. Not real crime data.
 */
@Entity
@Table(
    name = "crime_records",
    indexes = {
        @Index(name = "idx_area",        columnList = "area"),
        @Index(name = "idx_crime_type",  columnList = "crime_type"),
        @Index(name = "idx_date",        columnList = "crime_date"),
        @Index(name = "idx_severity",    columnList = "severity"),
        @Index(name = "idx_year_month",  columnList = "year, month")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crime_id", unique = true, nullable = false, length = 20)
    private String crimeId;

    @Column(name = "crime_date", nullable = false)
    private LocalDate crimeDate;

    @Column(name = "crime_time", nullable = false)
    private LocalTime crimeTime;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(name = "day_of_week", nullable = false, length = 15)
    private String dayOfWeek;

    @Column(nullable = false)
    private Integer hour;

    @Column(nullable = false, length = 60)
    @NotBlank
    private String area;

    @Column(nullable = false, length = 30)
    private String zone;

    @Column(name = "crime_type", nullable = false, length = 50)
    @NotBlank
    private String crimeType;

    @Column(nullable = false, length = 10)
    @NotBlank
    private String severity;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "case_status", nullable = false, length = 25)
    private String caseStatus;

    // -------------------------------------------------------
    // Computed / transient helpers
    // -------------------------------------------------------

    /** Returns true if the crime occurred during night hours (20:00 – 05:00). */
    @Transient
    public boolean isNightCrime() {
        return hour >= 20 || hour <= 5;
    }

    /** Returns true if the crime occurred during peak evening hours (17:00 – 21:00). */
    @Transient
    public boolean isPeakEveningCrime() {
        return hour >= 17 && hour <= 21;
    }
}
