package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Indicator Entity
 * ===========================================================
 * 
 * This entity represents a metric or indicator used to evaluate countries 
 * in the Africa AI Ranking system. Each indicator contributes to the overall
 * score of a country based on its weight and is categorized for easier
 * analysis and interpretation.
 * 
 * Features:
 * - Support for different normalization types (MinMax, Z-Score, etc.)
 * - Weighted scoring system (1-100 scale)
 * - Detailed description field for transparency
 * - Bidirectional relationships with scores and categories
 * - Audit tracking with creation and modification timestamps
 * 
 * Key annotations:
 * - @Entity: Marks this class as a JPA entity
 * - @Table: Defines database table name and indexes
 * - @JsonManagedReference/@JsonBackReference: Handles bidirectional serialization
 * - @Builder: Enables fluent object creation
 * 
 * Important relationships:
 * - One-to-many with Score: Each indicator has multiple scores across countries
 * - Many-to-one with IndicatorCategory: Each indicator belongs to a category
 *
 * Usage example:
 *
 * // Creating a new indicator
 * Indicator indicator = Indicator.builder()
 *     .name("AI Research Publications")
 *     .description("Number of AI research papers published per million population")
 *     .normalizationType("MinMax Normalisation")
 *     .weight(75)
 *     .build();
 *
 * // Assigning to a category
 * indicator.setCategory(aiResearchCategory);
 * 
 * @since 1.0
 * @version 1.1
 */


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "indicator")
public class Indicator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The name of the indicator.
     * Must be unique, descriptive, and between 2-100 characters.
     * Examples: "AI Research Publications", "AI Startups per Capita"
     */
//    @NotBlank(message = "Indicator name is required")
//    @Size(min = 5, max = 100, message = "Indicator name must be between 5 and 100 characters")
//    @Column(unique = true, nullable = false)
    @Column(unique = true)
    private String name;
    
    /**
     * Detailed explanation of what the indicator measures and how.
     * Provides transparency about the data collection and interpretation.
     */
//    @NotBlank(message = "Indicator description is required")
//    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
//    @Column(length = 1000, nullable = false)
    private String description;
    
    /**
     * The method used to normalize raw indicator values.
     * Common types include "MinMax Normalisation", "Z-Score", "Percentile Rank".
     * Normalization ensures fair comparison across countries with different scales.
     */
//    @NotBlank(message = "Normalization type is required")
//    @Column(name = "normalization_type", nullable = false)
    private String normalizationType;
    
    /**
     * The relative importance of this indicator in the overall country ranking.
     * Higher weights give the indicator more influence on the final score.
     * Scale: 0 (minimal importance) to 100 (highest importance)
     */
//    @Min(value = 0, message = "Weight must be at least 0")
//    @Max(value = 100, message = "Weight cannot exceed 100")
//    @Column(nullable = false)
//    private int weight;
    
    /**
     * All scores for this indicator across different countries and years.
     */
    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "indicator-score")
    @Builder.Default
    private List<Score> scores = new ArrayList<>();

    /**
     * The category this indicator belongs to.
     * Enables grouping related indicators for easier analysis.
     * Examples: "Research", "Infrastructure", "Talent"
     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @ManyToOne
//    @JoinColumn(name = "category_id")
//    @JsonBackReference(value = "category")
//    private IndicatorCategory category;


    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "indicator-weight")
    private List<IndicatorWeight> weights = new ArrayList<>();
    
    /**
     * Timestamp when this indicator record was created.
     * Automatically set during entity creation.
     */
//    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    /**
     * Timestamp when this indicator record was last modified.
     * Automatically updated on each entity update.
     */
//    @Column(name = "last_modified_date")
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
    
    /**
     * Adds a score to this indicator and establishes the bidirectional relationship.
     * @param score The score to associate with this indicator
     */
    public void addScore(Score score) {
        scores.add(score);
        score.setIndicator(this);
    }
    
    /**
     * Removes a score from this indicator and clears the bidirectional relationship.
     * @param score The score to remove from this indicator
     */
    public void removeScore(Score score) {
        scores.remove(score);
        score.setIndicator(null);
    }

    public void addWeight(IndicatorWeight weight) {
        weights.add(weight);
        weight.setIndicator(this);
    }

    public void removeWeight(IndicatorWeight weight) {
        weights.remove(weight);
        weight.setIndicator(null);
    }
}