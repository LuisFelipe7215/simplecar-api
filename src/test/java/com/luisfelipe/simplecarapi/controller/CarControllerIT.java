package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.response.CarGetResponse;
import com.luisfelipe.simplecarapi.response.CarPostResponse;
import com.luisfelipe.simplecarapi.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class CarControllerIT {
    public static final String URL = "/v1/cars";
    @Value("${admin.username}")
    public String adminUsername;
    @Value("${admin.password}")
    public String adminPassword;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private FileUtils fileUtils;

    @Order(1)
    @Test
    @DisplayName("GET /v1/cars returns all cars when successful")
    @Sql(value = "/sql/init_two_cars.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/sql/clean_cars.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_ReturnsAllCars_WhenSuccessful() {
        var typeReference = new ParameterizedTypeReference<List<CarGetResponse>>() {};
        var responseEntity = testRestTemplate.exchange(URL, HttpMethod.GET, null, typeReference);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull().doesNotContainNull();

        responseEntity
                .getBody()
                .forEach(profileGetResponse ->
                        assertThat(profileGetResponse).hasNoNullFieldsOrProperties());
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
    }

    @Order(3)
    @Test
    @Sql(value = "/sql/init_one_car.sql")
    @DisplayName("GET /v1/cars/1 returns a car with the given id")
    void findById_ReturnsCar_WhenSuccessful() {
        var responseEntity = testRestTemplate
                .exchange(URL + "/{id}", HttpMethod.GET, null, CarGetResponse.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull().hasNoNullFieldsOrProperties();
    }

    @Order(4)
    @Test
    @Sql(value = "/sql/init_one_car.sql")
    @DisplayName("GET /v1/cars/99 throws NotFoundException 404 when car is not found")
    void findById_ThrowsNotFoundException_WhenCarIsNotFound() throws IOException {
        var expectedResponse = fileUtils.readResourceFile("car/get-response-car-404.json");

        var responseEntity = testRestTemplate
                .exchange(URL + "/{id}", HttpMethod.GET, null, String.class, 99);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();

        assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }

    @Order(5)
    @Test
    @DisplayName("POST /v1/car creates a car")
    void save_CreatesCar_WhenSuccessful() throws IOException {
        var request = fileUtils.readResourceFile("car/post-request-car-200.json");
        var carEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL, HttpMethod.POST, carEntity, CarPostResponse.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull().hasNoNullFieldsOrProperties();
    }

    @Order(6)
    @Test
    @DisplayName("PUT /v1/car updates a car when successful")
    @Sql(value = "/sql/init_one_car.sql")
    void update_UpdateCar_WhenSuccessful() throws Exception {
        var request = fileUtils.readResourceFile("car/put-request-car-204.json");
        var carEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL, HttpMethod.PUT, carEntity, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Order(7)
    @Test
    @DisplayName("PUT /v1/car throws NotFoundException when car is not found")
    @Sql(value = "/sql/init_one_car.sql")
    void update_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        var request = fileUtils.readResourceFile("car/put-request-car-404.json");
        var expectedResponse = fileUtils.readResourceFile("car/put-response-car-404.json");
        var carEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL, HttpMethod.PUT, carEntity, String.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }

    @Order(8)
    @Test
    @DisplayName("DELETE v1/car/1 removes a car when successful")
    @Sql(value = "/sql/init_one_car.sql")
    void delete_RemovesCar_WhenSuccessful() {
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/{id}", HttpMethod.DELETE, null, Void.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Order(9)
    @Test
    @DisplayName("DELETE v1/car/99 throws NotFoundException when car is not found")
    @Sql(value = "/sql/init_one_car.sql")
    void delete_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        var expectedResponse = fileUtils.readResourceFile("car/delete-response-car-404.json");
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/{id}", HttpMethod.DELETE, null, String.class, 99);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }

    private static HttpEntity<String> buildHttpEntity(String request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, httpHeaders);
    }
}
