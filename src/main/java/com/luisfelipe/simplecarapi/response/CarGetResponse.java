package com.luisfelipe.simplecarapi.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CarGetResponse {
    private Long id;
    private String type;
    private String brand;
    private String model;
    private Integer year;
    private Double price;
}
