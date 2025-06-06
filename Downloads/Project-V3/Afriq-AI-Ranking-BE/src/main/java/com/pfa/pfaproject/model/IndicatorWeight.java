package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndicatorWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int weight;

//    private int year;

    @ManyToOne
    @JsonBackReference(value="indicator-weight")
    private Indicator indicator;

    @ManyToOne
    @JsonBackReference(value = "dimensionWeight-indicatorWeight")
    private DimensionWeight dimensionWeight;
}
