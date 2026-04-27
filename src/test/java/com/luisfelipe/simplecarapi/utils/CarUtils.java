package com.luisfelipe.simplecarapi.utils;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.domain.Photo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CarUtils {

    public List<Car> newCarsList(){
        Car corolla = Car.builder().id(1L).type("SUV").brand("Toyota").model("Corolla").year(2022).price(new BigDecimal("20000.00")).build();
        Car civic = Car.builder().id(2L).type("SUV").brand("Honda").model("Civic").year(2022).price(new BigDecimal("20000.00")).build();
        Car golf = Car.builder().id(3L).type("SUV").brand("Volkswagen").model("Golf").year(2022).price(new BigDecimal("20000.00")).build();
        return new ArrayList<>(List.of(corolla, civic, golf));
    }

    public Car newCarToSave(){
        return Car.builder().id(1L).type("Sedan").brand("Honda").model("Accord").year(2025).price(new BigDecimal("60000.00")).build();
    }

    public Car newCarToDelete(){
        Photo photo1 = Photo.builder().id(1L).fileName("photo1.jpg").thumbnail(true).build();
        Photo photo2 = Photo.builder().id(2L).fileName("photo2.jpg").thumbnail(false).build();
        Car carToDelete = Car.builder()
                .id(1L)
                .type("SUV")
                .brand("Volkswagen")
                .model("T-Cross")
                .year(2025)
                .price(new BigDecimal("149290.00"))
                .photos(List.of(photo1, photo2))
                .build();

        photo1.setCar(carToDelete);
        photo2.setCar(carToDelete);

        return carToDelete;
    }
}
