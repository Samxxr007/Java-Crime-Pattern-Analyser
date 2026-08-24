package com.crimeanalyzer.controller;

import com.crimeanalyzer.model.CrimeRecord;
import com.crimeanalyzer.service.CrimeService;
import com.crimeanalyzer.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST controller for Crime Record CRUD and filtering operations.
 *
 * <p>Base path: {@code /api/crimes}
 *
 * <p>Uses proper HTTP status codes:
 * <ul>
 *   <li>200 OK — successful retrieval</li>
 *   <li>201 Created — successful creation</li>
 *   <li>204 No Content — successful deletion</li>
 *   <li>404 Not Found — record not found</li>
 *   <li>400 Bad Request — validation failure</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/crimes")
@CrossOrigin(origins = "*")
public class CrimeController {

    @Autowired
    private CrimeService crimeService;

    @Autowired
    private DatasetService datasetService;

    // -------------------------------------------------------
    // GET all / paginated
    // -------------------------------------------------------

    @GetMapping
    public ResponseEntity<Page<CrimeRecord>> getAllPaged(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(crimeService.findAllPaged(page, Math.min(size, 200)));
    }

    // -------------------------------------------------------
    // GET by ID
    // -------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<CrimeRecord> getById(@PathVariable Long id) {
        return crimeService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------
    // GET by single filter fields
    // -------------------------------------------------------

    @GetMapping("/area/{area}")
    public ResponseEntity<List<CrimeRecord>> getByArea(@PathVariable String area) {
        return ResponseEntity.ok(crimeService.findByArea(area));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CrimeRecord>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(crimeService.findByCrimeType(type));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<CrimeRecord>> getBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(crimeService.findBySeverity(severity));
    }

    // -------------------------------------------------------
    // GET with composite filters (for map and table)
    // -------------------------------------------------------

    @GetMapping("/filter")
    public ResponseEntity<List<CrimeRecord>> filter(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String crimeType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(crimeService.findByFilters(area, crimeType, severity, fromDate, toDate));
    }

    @GetMapping("/filter/paged")
    public ResponseEntity<Page<CrimeRecord>> filterPaged(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String crimeType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "50")  int size) {
        return ResponseEntity.ok(
            crimeService.findByFiltersPaged(area, crimeType, severity, fromDate, toDate,
                                             page, Math.min(size, 200)));
    }

    // -------------------------------------------------------
    // Dropdown data
    // -------------------------------------------------------

    @GetMapping("/distinct/areas")
    public ResponseEntity<List<String>> getDistinctAreas() {
        return ResponseEntity.ok(crimeService.getDistinctAreas());
    }

    @GetMapping("/distinct/types")
    public ResponseEntity<List<String>> getDistinctTypes() {
        return ResponseEntity.ok(crimeService.getDistinctCrimeTypes());
    }

    @GetMapping("/distinct/zones")
    public ResponseEntity<List<String>> getDistinctZones() {
        return ResponseEntity.ok(crimeService.getDistinctZones());
    }

    // -------------------------------------------------------
    // CRUD — Create, Update, Delete
    // -------------------------------------------------------

    @PostMapping
    public ResponseEntity<CrimeRecord> create(@RequestBody CrimeRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crimeService.create(record));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrimeRecord> update(@PathVariable Long id,
                                               @RequestBody CrimeRecord record) {
        return ResponseEntity.ok(crimeService.update(id, record));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        crimeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // Dataset management
    // -------------------------------------------------------

    @PostMapping("/dataset/reimport")
    public ResponseEntity<Map<String, Object>> reimportDataset() {
        int count = datasetService.forceReimport();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("message", "Dataset reimported successfully");
        resp.put("recordsImported", count);
        resp.put("datasetPath", datasetService.getDatasetPath());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/dataset/info")
    public ResponseEntity<Map<String, Object>> datasetInfo() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("datasetPath", datasetService.getDatasetPath());
        resp.put("totalDatabaseRecords", datasetService.getDatabaseCount());
        resp.put("disclaimer",
            "SYNTHETIC DATASET - Generated for academic demonstration only. " +
            "Does NOT represent official Chennai crime statistics.");
        return ResponseEntity.ok(resp);
    }
}
