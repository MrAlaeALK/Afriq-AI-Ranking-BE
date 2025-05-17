package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
 *
 * Categories enable:
 * - Better organization of indicators
 * - Filtering and visualization by thematic areas
 * - Comparing countries across specific domains
 *
 * @since 1.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "indicator_category")
public class IndicatorCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 5, max = 100, message = "Category name must be between 5 and 100 characters")
    @Column(unique = true, nullable = false)
    private String name;

    @NotBlank(message = "Category description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500, nullable = false)
    private String description;

    // Display order in UI ?
    @Min(value = 0, message = "Display order must be a positive number")
    @Column(name = "display_order")
    private Integer displayOrder;

    // Relationship with indicators
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    //@JsonManagedReference(value = "category")
    @Builder.Default
    @OrderBy("weight DESC")
    private List<Indicator> indicators = new ArrayList<>();

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

    // Helper methods
    public void addIndicator(Indicator indicator) {
        indicators.add(indicator);
        indicator.setCategory(this);
    }

    public void removeIndicator(Indicator indicator) {
        indicators.remove(indicator);
        indicator.setCategory(null);
    }
}