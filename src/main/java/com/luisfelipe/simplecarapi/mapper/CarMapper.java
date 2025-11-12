package com.luisfelipe.simplecarapi.mapper;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.request.CarPostRequest;
import com.luisfelipe.simplecarapi.request.CarPutRequest;
import com.luisfelipe.simplecarapi.response.CarGetResponse;
import com.luisfelipe.simplecarapi.response.CarPostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarMapper {

    List<CarGetResponse> toCarGetResponseList(List<Car> cars);

    CarGetResponse toCarGetResponse(Car car);

    @Mapping(target = "id", ignore = true)
    Car toCar(CarPostRequest carPostRequest);

    Car toCar(CarPutRequest carPutRequest);

    CarPostResponse toCarPostResponse(Car car);
}
