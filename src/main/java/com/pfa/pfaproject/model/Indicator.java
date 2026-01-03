package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
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

    @Column
    private String name;

    private String description;

    private String normalizationType;
    
    //All scores for this indicator across different countries and years.
    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "indicator-score")
    @Builder.Default
    private List<Score> scores = new ArrayList<>();

    @ManyToOne
    @JsonBackReference(value = "dimension-indicator")
    private Dimension dimension;

    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "indicator-weight")
    @Builder.Default
    private List<IndicatorWeight> weights = new ArrayList<>();

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

    public void addWeight(IndicatorWeight weight) {
        weights.add(weight);
        weight.setIndicator(this);
    }

    public void removeWeight(IndicatorWeight weight) {
        weights.remove(weight);
        weight.setIndicator(null);
    }

    // Get weight of the indicator for a given year
    public Integer getWeightForYear(Integer year) {
        return weights.stream()
                .filter(weight -> weight.getYear().equals(year))
                .findFirst()
                .map(IndicatorWeight::getWeight)
                .orElse(null);
    }

    // Get weight - returns first available weight (for method reference compatibility)
    // Note: Prefer using getWeightForYear(Integer year) for year-specific weights
    public Integer getWeight() {
        return weights.stream()
                .findFirst()
                .map(IndicatorWeight::getWeight)
                .orElse(null);
    }

    // Get years that contains weight
    public List<Integer> getAvailableYears() {
        return weights.stream()
                .map(IndicatorWeight::getYear)
                .filter(year -> year != null)  // Filter out null years
                .distinct()
                .sorted()
                .toList();
    }
}