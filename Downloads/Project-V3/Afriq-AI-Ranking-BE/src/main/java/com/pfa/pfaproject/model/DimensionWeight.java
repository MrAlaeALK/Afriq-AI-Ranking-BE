package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionWeight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int dimensionWeight;

    private int year;

    @ManyToOne
    @JsonBackReference(value="dimension-weight")
    private Dimension dimension;

    @OneToMany(mappedBy = "dimensionWeight", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "dimensionWeight-indicatorWeight")
    private List<IndicatorWeight> indicatorWeights;

}
