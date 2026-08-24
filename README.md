# Chennai Crime Pattern Analyzer & Recommender

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Leaflet](https://img.shields.io/badge/Leaflet-1.9.4-199900?style=for-the-badge&logo=leaflet&logoColor=white)](https://leafletjs.com/)
[![Chart.js](https://img.shields.io/badge/Chart.js-4.4.2-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white)](https://www.chartjs.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://getbootstrap.com/)

> **ACADEMIC DEMONSTRATION DISCLAIMER**  
> **The crime dataset utilized throughout this application is entirely SYNTHETIC and programmatically generated for academic demonstration purposes.**  
> It does **NOT** represent actual or official Chennai crime statistics, police records, or real individuals. No individual-level profiling or predictions are performed. All analytical indicators and recommendations are strictly location-level and prevention-oriented.

---

## 1. Problem Statement

Urban administrative bodies and community safety planners often face challenges in aggregating, visualizing, and interpreting spatial and temporal incident records. Raw data tables without interactive visual exploration tools make it difficult to identify peak incident windows, geographic patterns, or prioritize preventive safety infrastructure (such as lighting, surveillance, or patrol scheduling).

This capstone project delivers a comprehensive, full-stack Java web application tailored for Chennai city. It provides spatial mapping, temporal clustering, statistical distribution analysis, synthetic hotspot ranking, and rule-based preventive recommendations.

---

## 2. Project Objectives

1. **Interactive Spatial Visualization**: Map synthetic incident records across 25 Chennai localities using Leaflet.js with marker clustering and density heatmaps.
2. **Multi-Dimensional Filtering**: Enable real-time filtering across geographic area, crime category, severity, date range, and time of day.
3. **Statistical & Temporal Analytics**: Compute incident distribution across hours, days of the week, months, years, and crime categories using the Java Collections Framework and Streams API.
4. **Synthetic Hotspot Scoring**: Calculate location-level composite risk scores using frequency, severity weighting, and recent activity metrics.
5. **Location-Level Recommender Engine**: Generate contextual preventive measures and safety advisories tailored to specific area-crime combinations.
6. **Robust REST API Architecture**: Provide RESTful endpoints for spatial data, statistical summaries, filtering, and CRUD operations.
7. **Clean Object-Oriented Design**: Demonstrate core Java concepts including Encapsulation, Abstraction, Polymorphism, Inheritance, Exception Handling, File I/O, and JPA repository abstractions.

---

## 3. Technology Stack

### Backend
- **Core Platform**: Java 17 LTS
- **Framework**: Spring Boot 3.2.5 (Spring MVC, Spring Data JPA, Spring Validation)
- **Build & Dependency Management**: Apache Maven
- **Persistence Layer**: Hibernate 6 / JPA
- **Database Driver**: MySQL Connector/J 8.3
- **File Parsing & I/O**: OpenCSV 5.9, Java NIO
- **JSON Serialization**: Jackson (JSR-310 datetime module)
- **Boilerplate Reduction**: Project Lombok
- **Testing**: JUnit 5, Mockito, AssertJ

### Frontend
- **Templating**: Thymeleaf
- **Styling**: Bootstrap 5.3, Bootstrap Icons, Custom Responsive CSS
- **Interactive Mapping**: Leaflet.js 1.9.4, Leaflet MarkerCluster 1.5.3, Leaflet Heat 0.2.0
- **Data Visualization**: Chart.js 4.4.2
- **Client Scripting**: Vanilla JavaScript (ES6+ asynchronous fetch architecture)

### Database
- **Engine**: MySQL 8.0+ / MariaDB 10.5+

---

## 4. Architecture & Design Patterns

The project adheres to a clean **Model-View-Controller (MVC)** layered architecture:

```
[ Web Browser / Client ]
          │
          ▼
[ Thymeleaf Templates (HTML5 + Bootstrap 5 + JS) ]
          │
          │ HTTP / REST (JSON)
          ▼
[ Controller Layer ]
  ├── PageController             (HTML View routing)
  ├── CrimeController            (CRUD & Filter REST API)
  ├── AnalyticsController        (Distribution & Hotspot API)
  └── RecommendationController   (Prevention Recommender API)
          │
          ▼
[ Service Layer ]
  ├── CrimeService               (Business logic, validation)
  ├── AnalyticsService           (Aggregation, TreeMap/HashMap analytics, scoring)
  ├── RecommendationService      (Rule-based preventive logic)
  ├── DatasetService             (CSV lifecycle & auto-import)
  └── DatasetGenerator           (Reproducible synthetic generation)
          │
          ▼
[ Repository Layer ]
  └── CrimeRepository           (Spring Data JPA + JPQL Queries)
          │
          ▼
[ Database Layer ]
  └── MySQL Database (`crime_analyzer` -> `crime_records`)
```

### Design Patterns Applied:
- **Repository Pattern**: `CrimeRepository` abstracts database access behind JPA interfaces.
- **Service Layer Pattern**: Decouples business calculations from HTTP controllers.
- **Builder Pattern**: Fluent object instantiation for domain models (`CrimeRecord`, `Location`, `Recommendation`).
- **Strategy / Lookup Pattern**: `RecommendationService` selects context-aware prevention measures via key-indexed collections.
- **Global Controller Advice**: `GlobalExceptionHandler` intercepts exceptions and formats structured JSON responses.

---

## 5. Core Java & Object-Oriented Principles

| OOP Concept | Implementation in Project |
| :--- | :--- |
| **Encapsulation** | Model fields in `CrimeRecord`, `Location`, `AreaAnalysis`, and `CrimeStatistics` are declared `private` with explicit accessors/mutators managed via Lombok annotations (`@Getter`, `@Setter`, `@Data`). |
| **Abstraction** | Service layers abstract internal storage mechanisms; controllers interact with services through high-level interfaces and methods (`analyzeArea`, `getRecommendation`). |
| **Polymorphism** | Runtime exception dispatching via `@RestControllerAdvice` and `@ExceptionHandler` polymorphism across various exception types (`IllegalArgumentException`, `NoSuchElementException`). |
| **Builder Pattern** | Implemented using `@Builder` on entities and DTOs to ensure immutable and flexible object construction. |
| **File I/O & Streams** | `DatasetGenerator` and `DatasetService` utilize Java NIO (`Files`, `Paths`, `BufferedWriter`, `BufferedReader`) and OpenCSV for streaming CSV ingestion. |

---

## 6. Java Collections Framework & Algorithm Complexity

The system leverages specific Java Collection structures to optimize search, aggregation, and sorting operations:

```
┌────────────────────────┬───────────────────────────────────┬──────────────────────────────────────────┐
│ Collection Structure   │ Use Case in Project               │ Time / Space Complexity                  │
├────────────────────────┼───────────────────────────────────┼──────────────────────────────────────────┤
│ HashMap<String, Long>  │ Area & Crime Category counts      │ Insertion / Lookup: O(1) avg             │
│ TreeMap<Integer, Long> │ Chronological Hour / Month trends │ Insertion: O(log k), Key Order: O(1)     │
│ ArrayList<CrimeRecord> │ Batch filtering & Page extraction │ Append: O(1) amortized, Iteration: O(n)  │
│ HashSet<String>        │ Validation sets (Severities, etc.)│ Contains check: O(1)                     │
│ Java Streams API       │ Multi-tier aggregation pipelines  │ Pipeline processing: O(n)                │
└────────────────────────┴───────────────────────────────────┴──────────────────────────────────────────┘
```

### Key Algorithm Complexities:
- **Frequency Aggregation**: $\mathcal{O}(n)$ single-pass map accumulation over $n$ records.
- **Filter Query Execution**: $\mathcal{O}(n)$ index-assisted scan on composite conditions (`area`, `crimeType`, `severity`, `crimeDate`).
- **Hotspot Ranking**: $\mathcal{O}(k \log k)$ where $k$ is the number of distinct localities ($k = 25$).
- **Primary Key Lookups**: $\mathcal{O}(1)$ via B-Tree indexed primary keys.

---

## 7. Synthetic Hotspot Scoring Model

The application computes an academic, location-level **Composite Hotspot Score** for each Chennai area based on synthetic records:

$$\text{Hotspot Score} = (\text{normFreq} \times 0.50) + (\text{normSev} \times 0.30) + (\text{normRec} \times 0.20)$$

Where:
- $\text{normFreq} = \frac{\text{Area Incident Count}}{\max(\text{Global Area Counts})}$
- $\text{normSev} = \min\left(1.0, \frac{\text{High Severity Incidents}}{\text{Total Area Incidents}} \times 3.0\right)$
- $\text{normRec} = \min\left(1.0, \frac{\text{Recent Year Incidents}}{\text{Total Area Incidents}} \times 2.0\right)$

### Classification Thresholds:
- **HIGH**: $\text{Score} \ge 0.60$
- **MODERATE**: $0.35 \le \text{Score} < 0.60$
- **LOW**: $\text{Score} < 0.35$

---

## 8. Database Schema & Indexes

The application automatically creates and updates the `crime_records` table via Hibernate:

```sql
CREATE DATABASE IF NOT EXISTS crime_analyzer
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE crime_analyzer;

CREATE TABLE IF NOT EXISTS crime_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crime_id VARCHAR(20) NOT NULL UNIQUE,
    crime_date DATE NOT NULL,
    crime_time TIME NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    hour INT NOT NULL,
    area VARCHAR(60) NOT NULL,
    zone VARCHAR(30) NOT NULL,
    crime_type VARCHAR(50) NOT NULL,
    severity VARCHAR(10) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    description TEXT,
    case_status VARCHAR(25) NOT NULL,
    INDEX idx_area (area),
    INDEX idx_crime_type (crime_type),
    INDEX idx_date (crime_date),
    INDEX idx_severity (severity),
    INDEX idx_year_month (year, month)
) ENGINE=InnoDB;
```

---

## 9. Synthetic Dataset Details

- **File Path**: `dataset/chennai_crime_synthetic.csv`
- **Record Count**: 3,000 synthetic records
- **Reproducibility**: Generated via `DatasetGenerator.java` using a fixed random seed (`20240101L`).
- **Geographic Coverage**: 25 distinct Chennai localities across North, Central, South, East, and West zones:
  - *Anna Nagar, T Nagar, Adyar, Velachery, Tambaram, Guindy, Egmore, Mylapore, Perambur, Ambattur, Porur, Sholinganallur, Thoraipakkam, Chromepet, Pallavaram, Saidapet, Nungambakkam, Royapettah, Triplicane, Kodambakkam, Ashok Nagar, Vadapalani, Mogappair, Avadi, Red Hills*.
- **Crime Categories (10)**:
  - *Theft, Burglary, Robbery, Vehicle Theft, Cyber Crime, Fraud, Assault, Vandalism, Chain Snatching, Harassment*.
- **Severity Levels**: `LOW`, `MEDIUM`, `HIGH`
- **Case Statuses**: `REPORTED`, `UNDER_INVESTIGATION`, `CLOSED`

---

## 10. REST API Specification

### Crime Record Endpoints (`/api/crimes`)
| Method | Endpoint | Query Parameters | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/crimes` | `page`, `size` | Retrieve paginated crime records | `200 OK` |
| `GET` | `/api/crimes/{id}` | — | Fetch single crime record by ID | `200 OK` / `404 Not Found` |
| `GET` | `/api/crimes/filter` | `area`, `crimeType`, `severity`, `fromDate`, `toDate` | Filter records (full list for maps) | `200 OK` |
| `GET` | `/api/crimes/filter/paged` | `area`, `crimeType`, `severity`, `fromDate`, `toDate`, `page`, `size` | Paginated filtered records | `200 OK` |
| `GET` | `/api/crimes/distinct/areas` | — | Get list of distinct Chennai areas | `200 OK` |
| `GET` | `/api/crimes/distinct/types` | — | Get list of distinct crime types | `200 OK` |
| `POST`| `/api/crimes` | Request Body (`JSON`) | Create new crime record | `201 Created` |
| `PUT` | `/api/crimes/{id}` | Request Body (`JSON`) | Update existing crime record | `200 OK` |
| `DELETE`| `/api/crimes/{id}` | — | Delete crime record | `204 No Content` |
| `POST`| `/api/crimes/dataset/reimport` | — | Re-generate and re-import dataset | `200 OK` |
| `GET` | `/api/crimes/dataset/info` | — | Get dataset metadata & count | `200 OK` |

### Analytics Endpoints (`/api/analytics`)
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/analytics/summary` | Global summary statistics & distribution maps | `200 OK` |
| `GET` | `/api/analytics/by-area` | Total crime count mapped by area | `200 OK` |
| `GET` | `/api/analytics/by-type` | Total crime count mapped by category | `200 OK` |
| `GET` | `/api/analytics/by-month`| Chronological monthly distribution (1–12) | `200 OK` |
| `GET` | `/api/analytics/by-hour` | Chronological hourly distribution (0–23) | `200 OK` |
| `GET` | `/api/analytics/hotspots`| Ranked hotspot analysis for all areas | `200 OK` |
| `GET` | `/api/analytics/area/{area}` | Detailed analytical report for a specific area | `200 OK` / `404 Not Found` |

### Recommendation Endpoints (`/api/recommendations`)
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/recommendations` | Metadata & supported categories | `200 OK` |
| `GET` | `/api/recommendations/{area}/{crimeType}` | Prevention recommendation for area & crime type | `200 OK` |
| `GET` | `/api/recommendations/area/{area}` | All recommendations across categories for an area | `200 OK` |

---

## 11. Application User Interface

The web interface features an enterprise dashboard layout with responsive sidebar navigation:

1. **Executive Dashboard (`/dashboard`)**:
   - High-level metric summary cards (Total records, High severity cases, Dominant crime, Top area, Active areas, Closed cases).
   - 6 interactive Chart.js visualizations (Area Distribution, Crime Type Doughnut, Monthly Trend Line, Hourly Histogram, Severity Split, Day of Week breakdown).
   - Full synthetic hotspot classification table with visual progress indicators.

2. **Crime Records Table (`/records`)**:
   - Searchable, filterable tabular view with multi-criteria filters (Area, Type, Severity, Date range).
   - Dynamic client-side pagination with record count indicators.

3. **Chennai Crime Map (`/map`)**:
   - Interactive OpenStreetMap layer centered on Chennai city.
   - Dual visualization modes: **Clustered Markers** (with detailed popup inspection) and **Density Heatmap** (visualizing spatial intensity).

4. **Pattern Analysis (`/analysis`)**:
   - In-depth drilldown for individual Chennai localities with peak-time calculation, dominant category detection, and 5 dedicated charts.

5. **Prevention Recommendations (`/recommendations`)**:
   - Tailored, location-level crime prevention measures and advisories based on selected area and category patterns.

6. **About & Transparency (`/about`)**:
   - Complete academic dataset disclosures, system architecture overview, and algorithmic complexity reference.

---

## 12. Setup and Execution Guide

### Prerequisites
- **Java Development Kit (JDK)**: 17 or higher (`java -version`, `javac -version`)
- **MySQL Server**: 8.0 or higher
- **Apache Maven**: 3.8+ (or use standard Maven CLI)

### Step 1: Clone Repository
```bash
git clone https://github.com/Samxxr007/Java-Crime-Pattern-Analyser.git
cd Java-Crime-Pattern-Analyser
```

### Step 2: Database Configuration
1. Start your local MySQL service.
2. Create the database (optional if auto-creation is enabled in MySQL):
   ```sql
   CREATE DATABASE crime_analyzer;
   ```
3. Update `src/main/resources/application.properties` with your MySQL credentials, or export environment variables:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/crime_analyzer?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```

   *Alternatively, pass credentials via environment variables:*
   ```bash
   # Windows PowerShell
   $env:DB_URL="jdbc:mysql://localhost:3306/crime_analyzer?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_password"
   ```

### Step 3: Build Project
```bash
mvn clean install
```

### Step 4: Run Application
```bash
mvn spring-boot:run
```

*Or execute the packaged JAR directly:*
```bash
java -jar target/crime-pattern-analyzer-1.0.0.jar
```

### Step 5: Access Web Portal
Open your web browser and navigate to:
```
http://localhost:8080
```

> **Note on First Run**: On startup, `DatasetService` detects if the database is empty. It automatically generates `dataset/chennai_crime_synthetic.csv` (if not present) and populates the MySQL `crime_records` table with all 3,000 synthetic records. No manual data entry is required.

---

## 13. Running Automated Tests

Run unit test suites covering `CrimeService`, `AnalyticsService`, and `RecommendationService`:

```bash
mvn test
```

---

## 14. Project Directory Structure

```
Java-Crime-Pattern-Analyser/
│
├── dataset/
│   └── chennai_crime_synthetic.csv           # Pre-generated 3,000 synthetic records
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── crimeanalyzer/
│   │   │           ├── CrimeAnalyzerApplication.java       # Spring Boot main runner
│   │   │           │
│   │   │           ├── controller/                         # REST & View Controllers
│   │   │           │   ├── PageController.java
│   │   │           │   ├── CrimeController.java
│   │   │           │   ├── AnalyticsController.java
│   │   │           │   └── RecommendationController.java
│   │   │           │
│   │   │           ├── model/                              # Entities & Domain Models
│   │   │           │   ├── CrimeRecord.java
│   │   │           │   ├── Location.java
│   │   │           │   ├── CrimeStatistics.java
│   │   │           │   ├── AreaAnalysis.java
│   │   │           │   ├── HotspotResult.java
│   │   │           │   └── Recommendation.java
│   │   │           │
│   │   │           ├── repository/                         # Spring Data JPA Repository
│   │   │           │   └── CrimeRepository.java
│   │   │           │
│   │   │           ├── service/                            # Core Business & Analytics Logic
│   │   │           │   ├── CrimeService.java
│   │   │           │   ├── AnalyticsService.java
│   │   │           │   ├── RecommendationService.java
│   │   │           │   ├── DatasetService.java
│   │   │           │   └── DatasetGenerator.java
│   │   │           │
│   │   │           ├── exception/                          # Global Exception Handling
│   │   │           │   └── GlobalExceptionHandler.java
│   │   │           │
│   │   │           └── util/                               # Standalone utilities
│   │   │               └── StandaloneDataGenerator.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties                      # Configuration properties
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── style.css                           # Enterprise stylesheet
│   │       │   └── js/
│   │       │       ├── common.js                           # Shared JS utilities
│   │       │       ├── dashboard.js                        # Dashboard charts logic
│   │       │       ├── records.js                          # Table & pagination logic
│   │       │       ├── map.js                              # Leaflet map & clustering
│   │       │       ├── analysis.js                         # Area analysis charts
│   │       │       └── recommendations.js                  # Prevention recommendation logic
│   │       │
│   │       └── templates/
│   │           ├── index.html
│   │           ├── dashboard.html
│   │           ├── records.html
│   │           ├── map.html
│   │           ├── analysis.html
│   │           ├── recommendations.html
│   │           └── about.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── crimeanalyzer/
│                   └── service/
│                       ├── CrimeServiceTest.java
│                       ├── AnalyticsServiceTest.java
│                       └── RecommendationServiceTest.java
│
├── schema.sql                                              # MySQL DB schema definition
├── .gitignore
├── pom.xml                                                 # Maven dependencies & build configuration
└── README.md                                               # Comprehensive project documentation
```

---

## 15. Limitations & Future Scope

### Current Limitations:
- **Synthetic Data**: Records are simulated for academic demonstration and do not reflect official police intelligence.
- **Rule-Based Recommendations**: Advisories are generated via pattern-matching heuristics rather than real-time patrol optimization algorithms.
- **Static Coordinate Centroids**: Incidents are simulated around standard geographic centroids with localized jitter rather than address-level geocoding.

### Future Scope:
- Integration with GIS shapefiles for polygon-level administrative ward boundaries.
- Implementation of spatial clustering algorithms (DBSCAN / K-Means) in Java.
- Integration of role-based access control (RBAC) via Spring Security for administrative and law-enforcement dashboards.
- PDF and Excel analytical report export functionality via Apache POI and iText.

---

## 16. Academic Declaration

This project was developed as a college capstone project demonstrating Java programming, Object-Oriented Design, Collections Framework, Spring Boot, MySQL integration, and web-based data visualization.
