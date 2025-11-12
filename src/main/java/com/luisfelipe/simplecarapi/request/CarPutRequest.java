package com.luisfelipe.simplecarapi.request;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CarPutRequest {
    private Long id;
    private String type;
    private String brand;
    private String model;
    private Integer year;
    private Double price;
}
