package com.luisfelipe.simplecarapi.response;

import lombok.*;

import java.math.BigDecimal;

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
    private BigDecimal price;
}
