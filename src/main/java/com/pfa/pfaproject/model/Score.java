package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Score Entity
 * ===========================================================
 *
 * This entity represents a specific score for a country on a particular indicator
 * in a given year. It forms the fundamental data point for the Africa AI Ranking system.
 *
 * Features:
 * - Year-based historical data tracking
 * - Many-to-one relationships with both Country and Indicator
 * - Support for normalized score values
 *
 * Each score instance uniquely connects a country to an indicator for a specific year,
 * allowing for historical comparison and trend analysis over time.
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
@Table(name = "score")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;

    private Double rawValue;

//    @NotNull(message = "Year is required")
//    @Min(value = 2000, message = "Year must be at least 2000")
//    @Column(unique = false, nullable = false)
    private Integer  year;

    @ManyToOne
    @JsonBackReference(value = "country-score")
    private Country country;

    @ManyToOne
    @JsonBackReference(value = "indicator-score")
    private Indicator indicator;

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