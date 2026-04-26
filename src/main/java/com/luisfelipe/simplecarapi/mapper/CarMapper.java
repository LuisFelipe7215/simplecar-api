package com.luisfelipe.simplecarapi.mapper;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.request.CarPostRequest;
import com.luisfelipe.simplecarapi.request.CarPutRequest;
import com.luisfelipe.simplecarapi.response.CarGetResponse;
import com.luisfelipe.simplecarapi.response.CarListGetResponse;
import com.luisfelipe.simplecarapi.response.CarPostResponse;
import com.luisfelipe.simplecarapi.response.PhotoResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarMapper {

    CarGetResponse toCarGetResponse(Car car);

    @Mapping(target = "id", ignore = true)
    Car toCar(CarPostRequest carPostRequest);

    Car toCar(CarPutRequest carPutRequest);

    CarPostResponse toCarPostResponse(Car car);

    List<CarListGetResponse> toCarListGetResponseList(List<Car> cars);

    CarListGetResponse toCarListGetResponse(Car car);

    @AfterMapping
    default void setThumbnailAndUrl(Car car, @MappingTarget CarListGetResponse target) {
        car.getPhotos().stream()
                .filter(Photo::getThumbnail)
                .findFirst()
                .ifPresent(photo -> {
                    String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/v1/cars/photos/")
                            .path(photo.getFileName())
                            .toUriString();

                    target.setThumbnail(PhotoResponse.builder()
                            .id(photo.getId())
                            .url(url)
                            .thumbnail(true)
                            .build());
                });
    }
}
