package com.luisfelipe.simplecarapi.utils;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.domain.Photo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class PhotoUtils {

    public Car getCar() {
        return Car.builder().id(1L)
                .type("SUV")
                .brand("Volkswagen")
                .model("Golf")
                .year(2022)
                .price(new BigDecimal("20000.00"))
                .photos(new ArrayList<>()).build();
    }

    public Photo getPhoto(Car car){
        return Photo.builder()
                .id(1L)
                .fileName("test.jpg")
                .car(car)
                .thumbnail(true)
                .build();
    }

    public Car getCarWithMaxPhotos(){
        List<Photo> photos = new ArrayList<>();

        Car car = getCar();

        Photo photo;
        for (int i = 0; i < 5; i++) {
            photo = Photo.builder().id((long) i + 1).fileName("photo" + (i + 1) + ".jpg").thumbnail(false).car(car).build();

            if (i == 0){
                photo.setThumbnail(true);
            }

            photos.add(photo);
        }

        car.setPhotos(photos);

        return car;
    }
}
