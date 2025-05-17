package com.pfa.pfaproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Document Entity
 * ===========================================================
 * 
 * This entity represents annual ranking reports uploaded by administrators.
 * Each document provides analysis, methodology explanations, and detailed
 * rankings for African countries in the AI Readiness Index for a specific year.
 * 
 * Only one official document exists per year, serving as the authoritative
 * source for that year's ranking methodology and analysis.
 * 
 * @since 1.0
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Document title is required")
    @Column(nullable = false)
    private String title;
    
    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Column(nullable = false, unique = true)
    private Integer year;
    
    // Storage informations Part
    // Path to find our file
    @NotBlank(message = "File path is required")
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    /* Determine the file type
     * Some common values:
     *  -application/pdf for PDF files
     */
    @Column(name = "file_type")
    private String fileType;

    // Contain the original name of the uploaded file
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    // Admin who uploaded the document
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;
    
    // Audit fields
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}