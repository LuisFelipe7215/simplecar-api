package com.luisfelipe.simplecarapi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@With
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(nullable = false)
    private Boolean thumbnail;
    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
}
