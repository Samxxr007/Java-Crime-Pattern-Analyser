import os
import docx
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

from build_capstone_report_core import (
    set_cell_border, set_cell_background, format_run,
    add_para, add_bullet, add_main_heading, add_sub_heading,
    add_table_data, add_image_figure
)

def build_report():
    doc = Document()
    
    # Page setup: Standard A4 (8.27 x 11.69 inches), 1 inch margins all around
    section = doc.sections[0]
    section.page_width = Inches(8.27)
    section.page_height = Inches(11.69)
    section.top_margin = Inches(1.0)
    section.bottom_margin = Inches(1.0)
    section.left_margin = Inches(1.0)
    section.right_margin = Inches(1.0)
    
    # Ensure header and footer have no borders or divider lines
    section.header.is_linked_to_previous = False
    section.footer.is_linked_to_previous = False
    for h_or_f in [section.header, section.footer]:
        for p in h_or_f.paragraphs:
            pPr = p._element.pPr
            if pPr is not None:
                pBdr = pPr.find(qn('w:pBdr'))
                if pBdr is not None:
                    pPr.remove(pBdr)
    
    # =========================================================================
    # 1. TITLE PAGE
    # =========================================================================
    if os.path.exists('figures/college_logo.png'):
        p_logo = doc.add_paragraph()
        p_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_logo.paragraph_format.space_before = Pt(0)
        p_logo.paragraph_format.space_after = Pt(8)
        run_logo = p_logo.add_run()
        run_logo.add_picture('figures/college_logo.png', width=Inches(1.35))
    
    add_main_heading(doc, "CHENNAI CRIME PATTERN ANALYZER & RECOMMENDER", space_after=10, page_break=False)
    add_para(doc, "A CAPSTONE PROJECT REPORT", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=6)
    add_para(doc, "A Capstone Project Report submitted in partial fulfilment of the requirements for the Course of", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=6)
    add_para(doc, "CSA0910 PROGRAMMING IN JAVA", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=6)
    add_para(doc, "for the award of the degree of", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=6)
    add_para(doc, "BACHELOR OF TECHNOLOGY IN", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=4)
    add_para(doc, "COMPUTER SCIENCE AND ENGINEERING", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=14)
    
    add_para(doc, "Submitted by", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=4)
    add_para(doc, "SAMEER AHMED G (192421154)", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=14)
    
    add_para(doc, "Under the Supervision of", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=4)
    add_para(doc, "Dr. S. MAGESH KUMAR, M.E., Ph.D.", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=2)
    add_para(doc, "Professor / Program Director, CSE BIOSCIENCE", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=16)
    
    add_para(doc, "SIMATS ENGINEERING", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=2)
    add_para(doc, "Saveetha Institute of Medical and Technical Sciences", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=2)
    add_para(doc, "Saveetha Nagar, Thandalam, Chennai – 602 105", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=12)
    add_para(doc, "SEPTEMBER 2026", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_after=0)
    
    # =========================================================================
    # 2. BONAFIDE CERTIFICATE
    # =========================================================================
    add_para(doc, "SIMATS ENGINEERING", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_before=12, space_after=2)
    add_para(doc, "Saveetha Institute of Medical and Technical Sciences", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=2)
    add_para(doc, "Chennai – 602105", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=16)
    
    add_main_heading(doc, "BONAFIDE CERTIFICATE", space_after=16, page_break=True)
    
    cert_text = (
        "This is to certify that the Capstone Project entitled \"Chennai Crime Pattern Analyzer & Recommender\" "
        "is the bonafide work carried out by SAMEER AHMED G (Register No: 192421154) under my supervision and guidance, "
        "and is submitted in partial fulfilment of the requirements for the course CSA0910 Programming in Java for the award "
        "of the degree of Bachelor of Technology in Computer Science and Engineering at Saveetha School of Engineering, "
        "SIMATS, Chennai, during the academic year 2026."
    )
    add_para(doc, cert_text, align=WD_ALIGN_PARAGRAPH.JUSTIFY, space_after=28)
    
    # Signatures table
    sig_headers = ["COURSE COORDINATOR", "COURSE FACULTY / SUPERVISOR", "PROGRAM DIRECTOR"]
    sig_rows = [
        [
            "Dr. G. Charlyn Pushpa Latha\nProfessor / Dept of CSE\nSaveetha School of Engineering\nSIMATS, Chennai – 602105",
            "Dr. S. Magesh Kumar\nProfessor / Program Director\nCSE BIOSCIENCE\nSaveetha School of Engineering\nSIMATS, Chennai – 602105",
            "Dr. Malathi\nProgram Director\nSaveetha School of Engineering\nSIMATS, Chennai – 602105"
        ]
    ]
    add_table_data(doc, sig_headers, sig_rows, col_widths=[2.1, 2.3, 2.1])
    
    # =========================================================================
    # 3. DECLARATION
    # =========================================================================
    add_para(doc, "SIMATS ENGINEERING", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_before=12, space_after=2)
    add_para(doc, "Saveetha Institute of Medical and Technical Sciences", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=2)
    add_para(doc, "Chennai – 602105", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=16)
    
    add_main_heading(doc, "DECLARATION", space_after=16, page_break=True)
    
    dec_text = (
        "I, Sameer Ahmed G (Register No: 192421154), student of the Department of Computer Science and Engineering, "
        "SIMATS Engineering, Saveetha Institute of Medical and Technical Sciences, Chennai, hereby declare that the "
        "Capstone Project Work entitled \"Chennai Crime Pattern Analyzer & Recommender\" submitted in partial fulfilment "
        "of the requirements for the course CSA0910 Programming in Java is the result of my own bonafide research and development "
        "efforts. To the best of my knowledge, the work presented herein is original, robust, and has been executed in full "
        "adherence to the highest standards of engineering ethics and academic integrity.\n\n"
        "All references, literature citations, software libraries, and data formats utilized during this project have been "
        "explicitly acknowledged. I solemnly declare that the crime records dataset analyzed by this software has been "
        "synthetically synthesized using programmatic pseudo-random generation solely for academic demonstration and "
        "algorithmic validation. It does not reflect, represent, or infer official crime statistics of the Chennai City Police, "
        "nor does it involve personal profiling of any individuals or demographic communities.\n\n"
        "I confirm that all applicable ethical, academic, institutional, and professional guidelines established by SIMATS "
        "have been rigorously maintained throughout the design, software development, testing, and documentation stages."
    )
    add_para(doc, dec_text, align=WD_ALIGN_PARAGRAPH.JUSTIFY, space_after=24)
    
    add_para(doc, "Place: Chennai", align=WD_ALIGN_PARAGRAPH.LEFT, space_after=4)
    add_para(doc, "Date:  September 2026", align=WD_ALIGN_PARAGRAPH.LEFT, space_after=18)
    add_para(doc, "SAMEER AHMED G (192421154)", align=WD_ALIGN_PARAGRAPH.RIGHT, bold=True, space_after=4)
    add_para(doc, "Signature of the Candidate", align=WD_ALIGN_PARAGRAPH.RIGHT, italic=True, space_after=0)
    
    # =========================================================================
    # 4. INDIVIDUAL CONTRIBUTION STATEMENT
    # =========================================================================
    add_para(doc, "SIMATS ENGINEERING", align=WD_ALIGN_PARAGRAPH.CENTER, bold=True, space_before=12, space_after=2)
    add_para(doc, "Saveetha Institute of Medical and Technical Sciences", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=2)
    add_para(doc, "Chennai – 602105", align=WD_ALIGN_PARAGRAPH.CENTER, space_after=16)
    
    add_main_heading(doc, "INDIVIDUAL CONTRIBUTION STATEMENT", space_after=6, page_break=True)
    add_para(doc, "(Mandatory for Capstone Projects)", align=WD_ALIGN_PARAGRAPH.CENTER, italic=True, space_after=14)
    
    t0_headers = ["Student Name / Register No.", "Specific Responsibilities", "Design & Development Contribution", "Testing & Analysis Contribution", "Report Contribution", "Approx. Contribution (%)"]
    t0_rows = [
        [
            "Sameer Ahmed G\n(192421154)",
            "End-to-End System Architecture, Spring Boot Backend, MySQL/H2 Schema, Leaflet.js Mapping, Chart.js Visualizations",
            "Architected MVC layered structure; implemented JPA entities, JPQL queries, DatasetGenerator, and Recommender logic",
            "Authored 21 JUnit 5 & Mockito test suites; validated spatial hotspot formula, data integrity, and API performance",
            "Authored all report chapters, architectural diagrams, mathematical formulations, and outcome mapping tables",
            "100%"
        ],
        [
            "Total",
            "—",
            "—",
            "—",
            "—",
            "100%"
        ]
    ]
    add_table_data(doc, t0_headers, t0_rows, col_widths=[1.2, 1.2, 1.3, 1.3, 1.1, 0.7], table_title="Table 0: Individual Contribution Statement")
    
    # =========================================================================
    # 5. ABSTRACT
    # =========================================================================
    add_main_heading(doc, "ABSTRACT", space_after=14, page_break=True)
    
    abs_text = (
        "Rapid urbanization and expanding metropolitan infrastructures in modern Indian cities such as Chennai pose substantial "
        "challenges to public safety administration, law enforcement resource optimization, and urban governance. Traditional "
        "incident logging systems often maintain incident histories in isolated relational databases or static spreadsheets without "
        "providing dynamic spatial visualization, interactive temporal pattern extraction, or automated prevention advisory tools. "
        "To resolve these operational and analytical limitations, this capstone project designs, develops, and evaluates a full-stack, "
        "production-grade engineering solution titled the \"Chennai Crime Pattern Analyzer & Recommender\".\n\n"
        "The application is engineered using Java 17 and Spring Boot 3.2.5 within a clean Model-View-Controller (MVC) paradigm. "
        "Data persistence is realized via Spring Data JPA and Hibernate, supporting both MySQL 8.0 for production deployments and "
        "an in-memory H2 database for zero-configuration execution. A robust synthetic data generator synthesizes 3,000 realistic "
        "incident records across 25 prominent Chennai municipal localities spanning five administrative zones (North, Central, South, "
        "East, and West) and ten distinct incident classifications with diurnal and day-of-week probability distributions. "
        "The analytical core leverages the Java Collections Framework (HashMap, TreeMap, ArrayList, and HashSet) and the Java Streams API "
        "to compute single-pass frequency aggregations (O(n)), chronological hourly trends (O(k log k)), and multi-criteria risk ranking.\n\n"
        "A composite mathematical hotspot model calculates a normalized risk index based on incident volume (50% weight), severity "
        "severity weighting (30% weight), and recent temporal activity (20% weight), classifying localities into High, Moderate, "
        "and Low risk tiers. The client interface combines Bootstrap 5, Leaflet.js with marker clustering and spatial heatmaps, and "
        "Chart.js for analytical trend rendering. Furthermore, a heuristic recommendation engine provides tailored, location-level "
        "preventive safety measures without demographic profiling. Verification through 21 comprehensive JUnit 5 and Mockito automated "
        "tests demonstrates 100% test passage, sub-70 ms API latency, and reliable spatial analysis, establishing the platform as an "
        "effective academic and operational prototype."
    )
    add_para(doc, abs_text, align=WD_ALIGN_PARAGRAPH.JUSTIFY, space_after=14)
    
    add_para(doc, "Keywords: Crime Pattern Analysis, Spatial Hotspot Scoring, Java Collections Framework, Spring Boot, Leaflet.js Mapping, Location-Level Recommendations.", align=WD_ALIGN_PARAGRAPH.LEFT, bold=True, space_after=16)
    
    # =========================================================================
    # 6. TABLE OF CONTENTS
    # =========================================================================
    add_main_heading(doc, "TABLE OF CONTENTS", space_after=14, page_break=True)
    
    t1_headers = ["Sl. No.", "Title", "Page No."]
    t1_rows = [
        ["i", "BONAFIDE CERTIFICATE", "ii"],
        ["ii", "DECLARATION BY THE CANDIDATE", "iii"],
        ["iii", "INDIVIDUAL CONTRIBUTION STATEMENT", "iv"],
        ["iv", "ABSTRACT", "v"],
        ["v", "LIST OF FIGURES", "vii"],
        ["vi", "LIST OF TABLES", "viii"],
        ["vii", "LIST OF ABBREVIATIONS / SYMBOLS", "ix"],
        ["1", "CHAPTER 1: INTRODUCTION AND ENGINEERING PROBLEM", "1"],
        ["1.1", "Background", "1"],
        ["1.2", "Need for the Project", "1"],
        ["1.3", "Problem Statement", "2"],
        ["1.4", "Project Aim", "2"],
        ["1.5", "Project Objectives", "3"],
        ["1.6", "Scope (Inclusions, Exclusions & Boundary Conditions)", "3"],
        ["1.7", "Expected Engineering Outcomes", "4"],
        ["2", "CHAPTER 2: LITERATURE REVIEW AND EXISTING SOLUTIONS", "5"],
        ["2.1", "Review Method", "5"],
        ["2.2", "Review of Existing Work & Modern Technologies", "5"],
        ["2.3", "Comparative Analysis of Existing Solutions", "6"],
        ["2.4", "Research and Engineering Gap", "7"],
        ["2.5", "Proposed Contribution & Technical Novelty", "7"],
        ["3", "CHAPTER 3: ENGINEERING DESIGN, TRADE-OFFS, SAFETY & RISK", "8"],
        ["3.1", "Stakeholder and System Requirements", "8"],
        ["3.2", "Engineering Constraints and Applicable Standards", "9"],
        ["3.3", "Design Specifications & Technical Performance Targets", "10"],
        ["3.4", "Alternative Solutions and Decision Matrix Evaluation", "11"],
        ["3.5", "Selected System Architecture and Detailed Design", "12"],
        ["3.6", "Trade-off Analysis and Decision Rationale", "15"],
        ["3.7", "Sustainability, Ethics, Health & Safety, Security and Risk", "16"],
        ["4", "CHAPTER 4: IMPLEMENTATION, TESTING & PROJECT MANAGEMENT", "18"],
        ["4.1", "Development Approach and Computational Resources", "18"],
        ["4.2", "Software Implementation and Modern Tools Used", "18"],
        ["4.3", "Verification, Testing Plan and Validation Criteria", "21"],
        ["4.4", "Empirical Results and Analytical Visualizations", "23"],
        ["4.5", "Data Analysis, Statistical Inferences & Engineering Judgment", "25"],
        ["4.6", "Comparison with Existing Solutions & Objective Fulfillment", "26"],
        ["4.7", "Technical Strengths and Practical Limitations", "27"],
        ["4.8", "Project Planning, Lifecycle Timeline, Roles and Budget", "28"],
        ["5", "CHAPTER 5: CONCLUSION, FUTURE WORK AND REFLECTION", "30"],
        ["5.1", "Conclusion", "30"],
        ["5.2", "Future Work and Technological Enhancements", "30"],
        ["5.3", "Professional Learning, Skills and Individual Reflection", "31"],
        ["6", "REFERENCES", "33"],
        ["7", "APPENDICES (Appendix A to J)", "35"],
        ["8", "CAPSTONE PROJECT OUTCOME MAPPING SHEET", "42"]
    ]
    add_table_data(doc, t1_headers, t1_rows, col_widths=[0.9, 5.0, 0.9], table_title="Table 1: Table of Contents")
    
    # =========================================================================
    # 7. LIST OF FIGURES
    # =========================================================================
    add_main_heading(doc, "LIST OF FIGURES", space_after=14, page_break=True)
    
    t2_headers = ["Figure No.", "Figure Name", "Page No."]
    t2_rows = [
        ["Figure 3.1", "Multi-Tier Layered Architecture and Subsystem Data Flow", "13"],
        ["Figure 3.2", "Spatial Distance Calculation & Hotspot Scoring Algorithmic Pipeline", "15"],
        ["Figure 4.1", "Four-Panel Synthetic Incident Distribution and Trend Visualizations", "24"],
        ["Figure 4.2", "Interactive Leaflet.js Geospatial Visualization with Density Heatmap Mode", "25"],
        ["Figure 4.3", "Dynamic Area-Level Pattern Analysis Panel with Hourly Histograms", "26"],
        ["Figure 4.4", "Prevention Recommendations Panel for Selected Chennai Localities", "27"],
        ["Figure 4.5", "Project Execution Gantt Chart (12-Week Milestone Timeline)", "29"],
        ["Figure B.1", "Entity-Relationship (ER) Relational Database Schema Diagram", "37"]
    ]
    add_table_data(doc, t2_headers, t2_rows, col_widths=[1.2, 4.7, 0.9], table_title="Table 2: List of Figures")
    
    # =========================================================================
    # 8. LIST OF TABLES
    # =========================================================================
    add_main_heading(doc, "LIST OF TABLES", space_after=14, page_break=True)
    
    t3_headers = ["Table No.", "Table Name", "Page No."]
    t3_rows = [
        ["Table 0", "Individual Contribution Statement", "iv"],
        ["Table 1", "Table of Contents", "vi"],
        ["Table 2", "List of Figures", "vii"],
        ["Table 3", "List of Tables", "viii"],
        ["Table 4", "List of Abbreviations and Mathematical Symbols", "ix"],
        ["Table 5", "Comparative Analysis of Existing Crime Mapping and Analytics Solutions", "6"],
        ["Table 6", "Stakeholder and User Requirements Specification", "8"],
        ["Table 7", "Functional and Non-Functional System Requirements", "9"],
        ["Table 8", "Engineering Constraints and Applicable International Standards", "10"],
        ["Table 9", "Engineering Design Specifications and Performance Targets", "11"],
        ["Table 10", "Alternative Architectural Solutions Decision Matrix", "12"],
        ["Table 11", "Architectural Trade-off Analysis and Engineering Rationale", "16"],
        ["Table 12", "Sustainability, Ethics, Health & Safety, and Security Evaluation", "17"],
        ["Table 13", "Project Risk Assessment Register and Mitigation Strategies", "17"],
        ["Table 14", "Modern Engineering Tools, Software, and Frameworks Implemented", "20"],
        ["Table 15", "System Verification, Test Plan, and Automated Test Execution Results", "22"],
        ["Table 16", "Comparison with Benchmark Objectives and Achievement Matrix", "27"],
        ["Table 17", "Project Planning, Roles, Responsibilities, and Financial Budget", "29"],
        ["Table 18", "ABET / Institutional Student Outcome (SO1–SO7) Mapping Sheet", "42"]
    ]
    add_table_data(doc, t3_headers, t3_rows, col_widths=[1.2, 4.7, 0.9], table_title="Table 3: List of Tables")
    
    # =========================================================================
    # 9. LIST OF ABBREVIATIONS / SYMBOLS
    # =========================================================================
    add_main_heading(doc, "LIST OF ABBREVIATIONS / SYMBOLS", space_after=14, page_break=True)
    
    t4_headers = ["Symbol / Abbreviation", "Description", "Engineering Domain / Unit"]
    t4_rows = [
        ["API", "Application Programming Interface", "Web & Software Engineering"],
        ["CSV", "Comma-Separated Values", "Data Storage & Ingestion Format"],
        ["DTO", "Data Transfer Object", "Object-Oriented Design Pattern"],
        ["GIS", "Geographic Information System", "Geospatial Information Technology"],
        ["H2", "In-Memory Relational Database Management System", "Embedded Persistence Layer"],
        ["HTTP", "Hypertext Transfer Protocol (RFC 7231)", "Application Layer Protocol"],
        ["JPA", "Java Persistence API (Jakarta Persistence)", "Object-Relational Mapping (ORM)"],
        ["JPQL", "Java Persistence Query Language", "Declarative Entity Querying"],
        ["JVM", "Java Virtual Machine", "Runtime Execution Environment"],
        ["MVC", "Model-View-Controller", "Software Architecture Pattern"],
        ["NIO", "New Input/Output (Non-blocking I/O)", "Java High-Performance File Handling"],
        ["OOP", "Object-Oriented Programming", "Programming Paradigm"],
        ["REST", "Representational State Transfer", "Web Service Architecture"],
        ["SO", "Student Outcome (ABET Accreditation Criteria 1–7)", "Engineering Educational Metric"],
        ["d", "Haversine Great-Circle Spatial Distance", "Kilometers (km)"],
        ["R", "Mean Radius of the Earth", "Constant (6,371 km)"],
        ["f_norm", "Normalized Incident Frequency Ratio", "Dimensionless Metric [0.0, 1.0]"],
        ["w_sev", "Normalized Severity Score Weighting", "Dimensionless Metric [0.0, 1.0]"],
        ["r_rec", "Normalized Temporal Recency Activity Ratio", "Dimensionless Metric [0.0, 1.0]"],
        ["S_hotspot", "Composite Location-Level Hotspot Score", "Dimensionless Index [0.0, 1.0]"]
    ]
    add_table_data(doc, t4_headers, t4_rows, col_widths=[1.5, 3.8, 1.5], table_title="Table 4: List of Abbreviations and Mathematical Symbols")
    
    return doc

print("Part 1 built successfully.")
