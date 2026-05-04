package com.luisfelipe.simplecarapi.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CarPostRequest {
    @NotBlank(message = "Type cannot be null or blank")
    private String type;
    @NotBlank(message = "Brand cannot be null or blank")
    private String brand;
    @NotBlank(message = "Model cannot be null or blank")
    private String model;
    @NotNull(message = "Year cannot be null")
    private Integer year;
    @NotNull(message = "Type cannot be null")
    private BigDecimal price;
}
