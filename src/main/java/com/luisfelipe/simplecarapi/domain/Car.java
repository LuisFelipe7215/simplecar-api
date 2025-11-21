package com.luisfelipe.simplecarapi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private String model;
    @Column(name = "model_year", nullable = false)
    private Integer year;
    @Column(nullable = false)
    private Double price;
}
