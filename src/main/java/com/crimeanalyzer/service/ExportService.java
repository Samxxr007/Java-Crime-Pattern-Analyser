package com.crimeanalyzer.service;

import com.crimeanalyzer.model.CrimeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for exporting filtered crime records into standard CSV format.
 */
@Service
public class ExportService {

    @Autowired
    private CrimeService crimeService;

    /**
     * Writes filtered crime records into a PrintWriter stream in CSV format.
     */
    public void exportToCsv(PrintWriter writer, String area, String crimeType,
                            String severity, LocalDate fromDate, LocalDate toDate) {
        List<CrimeRecord> records = crimeService.findByFilters(area, crimeType, severity, fromDate, toDate);

        writer.println("# CHENNAI CRIME PATTERN ANALYZER - EXPORTED RECORD DATASET");
        writer.println("# Synthetic Academic Data - Location-level Analytics Only");
        writer.println("Crime ID,Date,Time,Year,Month,Day of Week,Hour,Area,Zone,Crime Type,Severity,Latitude,Longitude,Case Status,Description");

        for (CrimeRecord r : records) {
            writer.printf("%s,%s,%s,%d,%d,%s,%d,\"%s\",\"%s\",\"%s\",%s,%.6f,%.6f,%s,\"%s\"%n",
                    escape(r.getCrimeId()),
                    r.getCrimeDate() != null ? r.getCrimeDate().toString() : "",
                    r.getCrimeTime() != null ? r.getCrimeTime().toString() : "",
                    r.getYear() != null ? r.getYear() : 0,
                    r.getMonth() != null ? r.getMonth() : 0,
                    escape(r.getDayOfWeek()),
                    r.getHour() != null ? r.getHour() : 0,
                    escape(r.getArea()),
                    escape(r.getZone()),
                    escape(r.getCrimeType()),
                    escape(r.getSeverity()),
                    r.getLatitude() != null ? r.getLatitude() : 0.0,
                    r.getLongitude() != null ? r.getLongitude() : 0.0,
                    escape(r.getCaseStatus()),
                    escape(r.getDescription())
            );
        }
    }

    private String escape(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
