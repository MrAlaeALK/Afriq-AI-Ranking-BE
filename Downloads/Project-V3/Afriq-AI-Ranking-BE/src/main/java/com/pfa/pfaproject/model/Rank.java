package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Rank Entity
 * ===========================================================
 *
 * This entity represents a country's final ranking position in the Africa AI Ranking
 * system for a specific year. It stores both the calculated score and the ordinal
 * position relative to other countries.
 *
 * Features:
 * - Year-based historical rankings
 * - Final computed score storage
 * - Ordinal position tracking
 * - Many-to-one relationship with countries
 *
 * The rank value is determined by comparing finalScores across all countries
 * for a given year, with 1 being the highest rank (best performing country).
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
@Table(name = "rank")
public class Rank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Final score is required")
    @Column(name = "final_score", nullable = false)
    private double finalScore;

    @Min(value = 1, message = "Rank must be at least 1")
    @Column(nullable = false)
    private int rank;

    /* Could have a better year validation logic
     * The min year 2000 could be changed in future
     */
    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Column(nullable = false)
    private int year;

    /* Improve Performance
     * Loading a Rank entity doesn't
     *  immediately load Country entity
     */
//    @ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
//    @JoinColumn(name = "country_id", nullable = false)
    @JsonBackReference(value = "country-rank")
    private Country country;

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