package com.crimeanalyzer;

import com.crimeanalyzer.service.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main entry point for the Chennai Crime Pattern Analyzer application.
 *
 * <p>This application is a college capstone project that demonstrates:
 * <ul>
 *   <li>Java OOP principles (encapsulation, inheritance, polymorphism, abstraction)</li>
 *   <li>Java Collections Framework (ArrayList, HashMap, HashSet, TreeMap)</li>
 *   <li>Java Streams API</li>
 *   <li>Spring Boot MVC architecture</li>
 *   <li>JPA / Hibernate for database operations</li>
 *   <li>RESTful API design</li>
 *   <li>CSV file handling with OpenCSV</li>
 *   <li>Crime pattern analysis algorithms</li>
 *   <li>Location-level recommendation engine</li>
 * </ul>
 *
 * <p><strong>DISCLAIMER:</strong> All crime data used by this application is
 * completely synthetic and generated programmatically for academic demonstration
 * purposes only. It does NOT represent official Chennai crime statistics.
 */
@SpringBootApplication
public class CrimeAnalyzerApplication {

    private static final Logger log = LoggerFactory.getLogger(CrimeAnalyzerApplication.class);

    @Autowired
    private DatasetService datasetService;

    public static void main(String[] args) {
        SpringApplication.run(CrimeAnalyzerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initializeDataset() {
        return args -> {
            log.info("==========================================================");
            log.info("  Chennai Crime Pattern Analyzer — Academic Capstone Project");
            log.info("  DISCLAIMER: All data is SYNTHETIC for demonstration only.");
            log.info("==========================================================");
            datasetService.initializeDataset();
            log.info("Dataset initialization complete. Application is ready.");
            log.info("Open: http://localhost:8080");
        };
    }
}
