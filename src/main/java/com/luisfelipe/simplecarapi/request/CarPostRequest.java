package com.luisfelipe.simplecarapi.request;

import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CarPostRequest {
    private String type;
    private String brand;
    private String model;
    private Integer year;
    private BigDecimal price;
}
