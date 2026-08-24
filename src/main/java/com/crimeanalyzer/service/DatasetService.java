package com.crimeanalyzer.service;

import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.repository.CrimeRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle of the synthetic dataset — generation and database import.
 *
 * <p>On application startup, this service:
 * <ol>
 *   <li>Checks whether the CSV file exists; if not, generates it.</li>
 *   <li>Checks whether the database already has records; if not, imports the CSV.</li>
 * </ol>
 *
 * <p>This ensures the application works immediately after first launch.
 *
 * <p><strong>File I/O:</strong> Uses {@code java.nio.file} and OpenCSV for
 * robust CSV parsing with proper UTF-8 handling.
 */
@Service
public class DatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);

    @Value("${app.dataset.path:dataset/chennai_crime_synthetic.csv}")
    private String datasetPath;

    @Value("${app.dataset.auto-generate:true}")
    private boolean autoGenerate;

    @Value("${app.dataset.auto-import:true}")
    private boolean autoImport;

    @Value("${app.dataset.record-count:3000}")
    private int recordCount;

    @Autowired
    private DatasetGenerator datasetGenerator;

    @Autowired
    private CrimeRepository crimeRepository;

    /**
     * Main initialization method called on application startup.
     * Generates the CSV if absent; imports to database if empty.
     */
    public void initializeDataset() {
        Path csvPath = resolvePath();

        // Step 1: Generate CSV
        if (autoGenerate && !Files.exists(csvPath)) {
            log.info("CSV not found. Generating {} records…", recordCount);
            try {
                datasetGenerator.generate(csvPath, recordCount);
            } catch (IOException e) {
                log.error("Failed to generate dataset: {}", e.getMessage());
                return;
            }
        }

        // Step 2: Import to DB
        if (autoImport) {
            long existingCount = crimeRepository.count();
            if (existingCount == 0) {
                log.info("Database is empty. Importing CSV → MySQL…");
                importCsvToDatabase(csvPath);
            } else {
                log.info("Database already contains {} records. Skipping import.", existingCount);
            }
        }
    }

    /**
     * Force re-imports the dataset — clears existing records and re-imports.
     * Exposed via the REST API for demonstration purposes.
     */
    public int forceReimport() {
        Path csvPath = resolvePath();
        try {
            datasetGenerator.generate(csvPath, recordCount);
        } catch (IOException e) {
            log.error("Re-generation failed: {}", e.getMessage());
            return 0;
        }
        crimeRepository.deleteAll();
        return importCsvToDatabase(csvPath);
    }

    // -------------------------------------------------------
    // CSV import logic
    // -------------------------------------------------------

    /**
     * Parses the synthetic CSV and bulk-inserts records into MySQL.
     *
     * <p>Uses batch inserts ({@code saveAll}) for efficiency.
     * Lines starting with {@code #} are treated as comments/metadata and skipped.
     *
     * @param csvPath path to the CSV file
     * @return number of records imported
     */
    private int importCsvToDatabase(Path csvPath) {
        if (!Files.exists(csvPath)) {
            log.warn("CSV file not found at {}. Cannot import.", csvPath);
            return 0;
        }

        List<CrimeRecord> batch = new ArrayList<>();
        int totalImported = 0;
        int skipped = 0;
        final int BATCH_SIZE = 500;

        try (InputStream is = Files.newInputStream(csvPath);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader buffered = new BufferedReader(reader)) {

            // Skip comment lines that start with '#'
            // OpenCSV will handle the actual CSV header row
            String firstLine;
            List<String> preamble = new ArrayList<>();
            while ((firstLine = buffered.readLine()) != null && firstLine.startsWith("#")) {
                preamble.add(firstLine);
            }
            // firstLine now holds the header row — rebuild stream
        } catch (IOException e) {
            log.error("Error reading CSV preamble: {}", e.getMessage());
            return 0;
        }

        try (InputStream is = Files.newInputStream(csvPath);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] line;
            boolean headerFound = false;

            while ((line = csvReader.readNext()) != null) {
                // Skip comment lines and blank lines
                if (line.length == 0) continue;
                String first = line[0].trim();
                if (first.startsWith("#")) continue;

                // Skip header row
                if (!headerFound && first.equalsIgnoreCase("crime_id")) {
                    headerFound = true;
                    continue;
                }

                if (line.length < 15) {
                    skipped++;
                    continue;
                }

                try {
                    CrimeRecord record = parseLine(line);
                    if (!crimeRepository.existsByCrimeId(record.getCrimeId())) {
                        batch.add(record);
                    }

                    if (batch.size() >= BATCH_SIZE) {
                        crimeRepository.saveAll(batch);
                        totalImported += batch.size();
                        log.info("Imported {} records so far…", totalImported);
                        batch.clear();
                    }
                } catch (Exception e) {
                    skipped++;
                }
            }

            // Final batch
            if (!batch.isEmpty()) {
                crimeRepository.saveAll(batch);
                totalImported += batch.size();
            }

        } catch (IOException | CsvValidationException e) {
            log.error("CSV import error: {}", e.getMessage());
        }

        log.info("Import complete: {} records inserted, {} skipped.", totalImported, skipped);
        return totalImported;
    }

    private CrimeRecord parseLine(String[] f) {
        return CrimeRecord.builder()
            .crimeId(f[0].trim())
            .crimeDate(LocalDate.parse(f[1].trim()))
            .crimeTime(LocalTime.parse(f[2].trim()))
            .year(Integer.parseInt(f[3].trim()))
            .month(Integer.parseInt(f[4].trim()))
            .dayOfWeek(f[5].trim())
            .hour(Integer.parseInt(f[6].trim()))
            .area(f[7].trim())
            .zone(f[8].trim())
            .crimeType(f[9].trim())
            .severity(f[10].trim())
            .latitude(Double.parseDouble(f[11].trim()))
            .longitude(Double.parseDouble(f[12].trim()))
            .description(f[13].trim())
            .caseStatus(f[14].trim())
            .build();
    }

    private Path resolvePath() {
        return Paths.get(datasetPath).isAbsolute()
            ? Paths.get(datasetPath)
            : Paths.get(System.getProperty("user.dir")).resolve(datasetPath);
    }

    public String getDatasetPath() {
        return resolvePath().toAbsolutePath().toString();
    }

    public long getDatabaseCount() {
        return crimeRepository.count();
    }
}
