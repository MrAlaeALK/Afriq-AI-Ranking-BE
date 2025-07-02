package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IndicatorCategory Entity
 * ===========================================================
 *
 * This entity represents a logical grouping of related indicators in the
 * Africa AI Ranking system. Categories help organize indicators into
 * meaningful clusters such as "Infrastructure", "Education", "Research Output",
 * "Government Policy", etc.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "dimension", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "year"})
})
public class Dimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer year;

    private String description;

    // Display order in UI ?
    @Min(value = 0, message = "Display order must be a positive number")
    @Column(name = "display_order")
    private Integer displayOrder;

    @OneToMany(mappedBy = "dimension", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "dimension-weight")
    private List<DimensionWeight> weights = new ArrayList<>();

    @OneToMany(mappedBy="dimension", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "dimension-dimensionScore")
    private List<DimensionScore> dimensionScores = new ArrayList<>();

    @OneToMany(mappedBy = "dimension", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "dimension-indicator")
    @Builder.Default
    private List<Indicator> indicators = new ArrayList<>();

    private LocalDateTime createdDate;

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

    public void addWeight(DimensionWeight weight) {
        weights.add(weight);
        weight.setDimension(this);
    }

    /**
     * Adds an indicator to this dimension and establishes the bidirectional relationship.
     * @param indicator The indicator to associate with this dimension
     */
    public void addIndicator(Indicator indicator) {
        indicators.add(indicator);
        indicator.setDimension(this);
    }

    /**
     * Removes an indicator from this dimension and clears the bidirectional relationship.
     * @param indicator The indicator to remove from this dimension
     */
    public void removeIndicator(Indicator indicator) {
        indicators.remove(indicator);
        indicator.setDimension(null);
    }

    /**
     * Adds a dimension score to this dimension and establishes the bidirectional relationship.
     * @param dimensionScore The dimension score to associate with this dimension
     */
    public void addDimensionScore(DimensionScore dimensionScore) {
        dimensionScores.add(dimensionScore);
        dimensionScore.setDimension(this);
    }

    /**
     * Removes a dimension score from this dimension and clears the bidirectional relationship.
     * @param dimensionScore The dimension score to remove from this dimension
     */
    public void removeDimensionScore(DimensionScore dimensionScore) {
        dimensionScores.remove(dimensionScore);
        dimensionScore.setDimension(null);
    }
}