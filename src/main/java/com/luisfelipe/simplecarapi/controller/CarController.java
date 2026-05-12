package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.mapper.CarMapper;
import com.luisfelipe.simplecarapi.request.CarPostRequest;
import com.luisfelipe.simplecarapi.request.CarPutRequest;
import com.luisfelipe.simplecarapi.response.CarGetResponse;
import com.luisfelipe.simplecarapi.response.CarListGetResponse;
import com.luisfelipe.simplecarapi.response.CarPostResponse;
import com.luisfelipe.simplecarapi.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/cars")
@RequiredArgsConstructor
@Log4j2
public class CarController {
    private final CarService service;
    private final CarMapper mapper;

    @GetMapping
    public ResponseEntity<List<CarListGetResponse>> findAll() {
        log.debug("Request received to find all cars");

        var cars = service.findAll();

        var carGetResponseList = mapper.toCarListGetResponseList(cars);

        return ResponseEntity.ok(carGetResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarGetResponse> findById(@PathVariable Long id) {
        log.debug("Request received to find car by id: '{}'", id);

        var car = service.findById(id);

        var carGetResponse = mapper.toCarGetResponse(car);

        return ResponseEntity.ok(carGetResponse);
    }

    @PostMapping
    public ResponseEntity<CarPostResponse> save(@RequestBody @Valid CarPostRequest carPostRequest) {
        log.debug("Request received to save car: '{}'", carPostRequest);

        var carToSave = mapper.toCar(carPostRequest);

        var savedCar = service.save(carToSave);

        var carPostResponse = mapper.toCarPostResponse(savedCar);

        return ResponseEntity.status(HttpStatus.CREATED).body(carPostResponse);
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid CarPutRequest carPutRequest) {
        log.debug("Request received to update car: '{}'", carPutRequest);

        var car = mapper.toCar(carPutRequest);

        service.update(car);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Request received to delete car by id: '{}'", id);

        service.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}