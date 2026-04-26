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

    @Mapping(target = "url", expression = "java(buildPhotoUrl(photo))")
    PhotoResponse toPhotoResponse(Photo photo);

    Car toCar(CarPutRequest carPutRequest);

    CarPostResponse toCarPostResponse(Car car);

    List<CarListGetResponse> toCarListGetResponseList(List<Car> cars);

    CarListGetResponse toCarListGetResponse(Car car);

    default String buildPhotoUrl(Photo photo) {
        if (photo == null) return null;
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/cars/photos/")
                .path(photo.getFileName())
                .toUriString();
    }

    @AfterMapping
    default void setThumbnailAndUrl(Car car, @MappingTarget CarListGetResponse target) {
        car.getPhotos().stream()
                .filter(Photo::getThumbnail)
                .findFirst()
                .ifPresent(photo -> {
                    String url = buildPhotoUrl(photo);
                    target.setThumbnail(PhotoResponse.builder()
                            .id(photo.getId())
                            .url(url)
                            .thumbnail(true)
                            .build());
                });
    }
}
