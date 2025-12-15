package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.exception.CustomNotFoundException;
import com.luisfelipe.simplecarapi.mapper.CarMapper;
import com.luisfelipe.simplecarapi.request.CarPostRequest;
import com.luisfelipe.simplecarapi.request.CarPutRequest;
import com.luisfelipe.simplecarapi.response.CarGetResponse;
import com.luisfelipe.simplecarapi.response.CarPostResponse;
import com.luisfelipe.simplecarapi.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/cars")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Car API", description = "Car api related endpoints")
public class CarController {
    private final CarService service;
    private final CarMapper mapper;

    @GetMapping
    @Operation(summary = "Get all cars", description = "Get all cars available in the system",
            responses = {
                    @ApiResponse(description = "Get all cars",
                            responseCode = "200",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = CarGetResponse.class))))
            })
    public ResponseEntity<List<CarGetResponse>> findAll() {
        log.debug("Request received to find all cars");

        var cars = service.findAll();

        var carGetResponseList = mapper.toCarGetResponseList(cars);

        return ResponseEntity.ok(carGetResponseList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by id",
            responses = {
                    @ApiResponse(description = "Get car by its id",
                            responseCode = "200",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CarGetResponse.class))),
                    @ApiResponse(description = "Car not found",
                            responseCode = "404",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CustomNotFoundException.class)))
            })
    public ResponseEntity<CarGetResponse> findById(@PathVariable Long id) {
        log.debug("Request received to find car by id: '{}'", id);

        var car = service.findById(id);

        var carGetResponse = mapper.toCarGetResponse(car);

        return ResponseEntity.ok(carGetResponse);
    }

    @PostMapping
    @Operation(summary = "Save a new car",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody
                    (description = "Request to save a new car",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarPostRequest.class))),
            responses = {
                    @ApiResponse(description = "New car is saved",
                            responseCode = "201",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CarPostResponse.class)))
            })
    public ResponseEntity<CarPostResponse> save(@RequestBody CarPostRequest carPostRequest) {
        log.debug("Request received to save car: '{}'", carPostRequest);

        var carToSave = mapper.toCar(carPostRequest);

        var savedCar = service.save(carToSave);

        var carPostResponse = mapper.toCarPostResponse(savedCar);

        return ResponseEntity.status(HttpStatus.CREATED).body(carPostResponse);
    }

    @PutMapping
    @Operation(summary = "Update a existing car",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody
                    (description = "Request to update a existing car",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarPutRequest.class))),
            responses = {
                    @ApiResponse(description = "Existing car is updated",
                            responseCode = "204"),
                    @ApiResponse(description = "Car not found",
                    responseCode = "404",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomNotFoundException.class)))
            })
    public ResponseEntity<Void> update(@RequestBody CarPutRequest carPutRequest) {
        log.debug("Request received to update car: '{}'", carPutRequest);

        var car = mapper.toCar(carPutRequest);

        service.update(car);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car by id",
            responses = {
                    @ApiResponse(description = "Delete car by its id",
                            responseCode = "204"),
                    @ApiResponse(description = "Car not found",
                            responseCode = "404",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CustomNotFoundException.class)))
            })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Request received to delete car by id: '{}'", id);

        service.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
