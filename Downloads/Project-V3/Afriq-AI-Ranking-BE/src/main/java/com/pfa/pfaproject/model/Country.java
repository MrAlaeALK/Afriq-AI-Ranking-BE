package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Country Entity
 * ===========================================================
 *
 * This entity represents an African country in the Afriq'AI Ranking system.
 * Countries are the primary entities being evaluated and ranked based on
 * various AI-related indicators.
 *
 * Features:
 * - Comprehensive validation for all fields
 * - Bidirectional relationships with Score and Rank entities
 * - Helper methods for relationship management
 * - Indexed fields for optimized queries
 * - Audit tracking with creation and modification timestamps
 *
 * Key annotations:
 * - @Entity: Marks this class as a JPA entity
 * - @Table: Defines database table name and indexes
 * - @JsonManagedReference: Handles bidirectional serialization properly
 * - @Builder: Enables fluent object creation
 *
 * Important relationships:
 * - One-to-many with Rank: Each country has multiple historical rank entries
 * - One-to-many with Score: Each country has multiple indicator scores
 *
 * Usage example:
 *
 * // Creating a new country
 * Country country = Country.builder()
 *     .name("Ghana")
 *     .code("GH")
 *     .region("West Africa")
 *     .build();
 *
 * // Adding a score to a country
 * Score score = new Score();
 * country.addScore(score);
 *
 * @since 1.0
 * @version 1.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "country")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The official name of the country.
     * Must be unique and between 2-100 characters.
     */
//    @NotBlank(message = "Country name is required")
//    @Size(min = 2, max = 100, message = "Country name must be between 2 and 100 characters")
//    @Column(unique = true, nullable = false)
    @Column(unique = true)
    private String name;
    
    /**
     * The ISO country code (2-3 letters).
     * Must be unique, uppercase, and follow standard country code format.
     */
//    @NotBlank(message = "Country code is required")
//    @Size(min = 2, max = 3, message = "Country code must be 2 or 3 characters")
//    @Pattern(regexp = "^[A-Z]{2,3}$", message = "Country code must be 2-3 uppercase letters")
//    @Column(unique = true, nullable = false, length = 3)
    @Column(unique = true)
    private String code;
    
    /**
     * The geographic region of Africa where the country is located.
     * Used for regional analysis and comparisons.
     */
//    @NotBlank(message = "Region is required")
//    @Column(nullable = false)
    private String region;
    
    /**
     * Historical ranking data for this country across different years.
     * Automatically sorted by year (descending) and rank (ascending).
     */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "country-rank")
    @Builder.Default
//    @OrderBy("year DESC, rank ASC")
    private List<Rank> rankingHistory = new ArrayList<>();
    
    /**
     * All indicator scores for this country across different years.
     */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "country-score")
    @Builder.Default
    private List<Score> scores = new ArrayList<>();

    /**
     * All Dimension scores for this country accross different years
     */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value="country-dimensionScore")
    @Builder.Default
    private List<DimensionScore> dimensionScores = new ArrayList<>();
    
    /**
     * Timestamp when this country record was created.
     * Automatically set during entity creation.
     */
//    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    /**
     * Timestamp when this country record was last modified.
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
     * Adds a rank to this country and establishes the bidirectional relationship.
     * @param rank The rank to associate with this country
     */
    public void addRank(Rank rank) {
        rankingHistory.add(rank);
        rank.setCountry(this);
    }

    /**
     * Removes a rank from this country and clears the bidirectional relationship.
     * @param rank The rank to remove from this country
     */
    public void removeRank(Rank rank) {
        rankingHistory.remove(rank);
        rank.setCountry(null);
    }

    /**
     * Adds a score to this country and establishes the bidirectional relationship.
     * @param score The score to associate with this country
     */
    public void addScore(Score score) {
        scores.add(score);
        score.setCountry(this);
    }

    /**
     * Removes a score from this country and clears the bidirectional relationship.
     * @param score The score to remove from this country
     */
    public void removeScore(Score score) {
        scores.remove(score);
        score.setCountry(null);
    }

    public void addDimensionScore(DimensionScore dimensionScore) {
        dimensionScores.add(dimensionScore);
        dimensionScore.setCountry(this);
    }

    public void removeDimensionScore(DimensionScore dimensionScore) {
        dimensionScores.remove(dimensionScore);
        dimensionScore.setCountry(null);
    }
}


