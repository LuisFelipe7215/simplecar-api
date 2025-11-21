package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.response.CarGetResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarControllerIT {
    public static final String URL = "/v1/cars";
    @Autowired
    private TestRestTemplate testRestTemplate;


    @Test
    @DisplayName("GET /v1/cars returns all cars when successful")
    @Sql(value = "/sql/init_two_cars.sql")
    @Order(1)
    void findAll_ReturnsAllCars_WhenSuccessful() {
        var typeReference = new ParameterizedTypeReference<List<CarGetResponse>>() {};
        var responseEntity = testRestTemplate.exchange(URL, HttpMethod.GET, null, typeReference);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull().doesNotContainNull();

        responseEntity
                .getBody()
                .forEach(profileGetResponse -> assertThat(profileGetResponse).hasNoNullFieldsOrProperties());
    }

    @Order(2)
    @Test
    @DisplayName("GET /v1/cars returns an empty list when no cars are found")
    void findAll_ReturnsEmptyList_WhenNoCarsAreFound() {
        var typeReference = new ParameterizedTypeReference<List<CarGetResponse>>() {};
        var responseEntity = testRestTemplate.exchange(URL, HttpMethod.GET, null, typeReference);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull().isEmpty();

        responseEntity
                .getBody()
                .forEach(carGetResponse -> assertThat(carGetResponse).hasNoNullFieldsOrProperties());
    }

    @Order(3)
    @Test
    @Sql(value = "/sql/init_one_car.sql")
    @DisplayName("GET /v1/cars/1 returns a car with the given id")
    void findById_ReturnsCar_WhenSuccessful() {
        var typeReference = new ParameterizedTypeReference<CarGetResponse>() {};
        var responseEntity = testRestTemplate.exchange(URL + "/{id}", HttpMethod.GET, null, typeReference, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull().hasNoNullFieldsOrProperties();
    }


}
