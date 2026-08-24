-- ============================================================
-- Chennai Crime Pattern Analyzer
-- Database Initialization Script
-- ============================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS crime_analyzer
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crime_analyzer;

-- Create crime_records table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Note: The Spring Boot application automatically populates this table
-- from dataset/chennai_crime_synthetic.csv on first startup.
