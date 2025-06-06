package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DimensionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double score;

    private int year;

    @ManyToOne
    @JsonBackReference(value = "dimension-dimensionScore")
    private Dimension dimension;

    @ManyToOne
    @JsonBackReference(value = "country-dimensionScore")
    private Country country;
}
