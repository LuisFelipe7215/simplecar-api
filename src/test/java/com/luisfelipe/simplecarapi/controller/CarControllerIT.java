package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.response.CarGetResponse;
import com.luisfelipe.simplecarapi.response.CarListGetResponse;
import com.luisfelipe.simplecarapi.response.CarPostResponse;
import com.luisfelipe.simplecarapi.response.PhotoResponse;
import com.luisfelipe.simplecarapi.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class CarControllerIT {
    public static final String URL = "/v1/cars";
    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private FileUtils fileUtils;
    @LocalServerPort
    private int port;

    @Order(1)
    @Test
    @DisplayName("GET /v1/cars returns all cars when successful")
    @Sql(scripts = {"/sql/init_two_cars.sql", "/sql/init_two_photos.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_ReturnsAllCars_WhenSuccessful() {
        var typeReference = new ParameterizedTypeReference<List<CarListGetResponse>>() {
        };
        var responseEntity = testRestTemplate.exchange(URL, HttpMethod.GET, null, typeReference);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<CarListGetResponse> cars = responseEntity.getBody();
        assertThat(cars).isNotNull().hasSize(2);

        CarListGetResponse corolla = cars.getFirst();
        String expectedCorollaUrl = "http://localhost:" + port + "/v1/cars/photos/corolla.jpg";
        assertThat(corolla.getId()).isEqualTo(1L);
        assertThat(corolla.getBrand()).isEqualTo("Toyota");
        assertThat(corolla.getModel()).isEqualTo("Corolla");
        assertThat(corolla.getType()).isEqualTo("SUV");
        assertThat(corolla.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000.00));
        assertThat(corolla.getYear()).isEqualTo(2022);
        assertThat(corolla.getThumbnail()).isNotNull();
        assertThat(corolla.getThumbnail().getUrl()).isEqualTo(expectedCorollaUrl);

        CarListGetResponse civic = cars.get(1);
        String expectedCivicUrl = "http://localhost:" + port + "/v1/cars/photos/civic.jpg";
        assertThat(civic.getId()).isEqualTo(2L);
        assertThat(civic.getBrand()).isEqualTo("Honda");
        assertThat(civic.getModel()).isEqualTo("Civic");
        assertThat(civic.getType()).isEqualTo("SUV");
        assertThat(civic.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000.00));
        assertThat(civic.getYear()).isEqualTo(2022);
        assertThat(civic.getThumbnail()).isNotNull();
        assertThat(civic.getThumbnail().getUrl()).isEqualTo(expectedCivicUrl);
    }

    @Order(2)
    @Test
    @DisplayName("GET /v1/cars returns an empty list when no cars are found")
    void findAll_ReturnsEmptyList_WhenNoCarsAreFound() {
        var typeReference = new ParameterizedTypeReference<List<CarListGetResponse>>() {
        };
        var responseEntity = testRestTemplate.exchange(URL, HttpMethod.GET, null, typeReference);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull().isEmpty();
    }

    @Order(3)
    @Test
    @Sql(scripts = {"/sql/init_one_car.sql", "/sql/init_five_photos.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("GET /v1/cars/1 returns a car with the given id")
    void findById_ReturnsCar_WhenSuccessful() {
        var responseEntity = testRestTemplate
                .exchange(URL + "/{id}", HttpMethod.GET, null, CarGetResponse.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarGetResponse corolla = responseEntity.getBody();
        assertThat(corolla).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(corolla.getId()).isEqualTo(1L);
        assertThat(corolla.getBrand()).isEqualTo("Toyota");
        assertThat(corolla.getModel()).isEqualTo("Corolla");
        assertThat(corolla.getType()).isEqualTo("SUV");
        assertThat(corolla.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000.00));
        assertThat(corolla.getYear()).isEqualTo(2022);

        List<PhotoResponse> corollaPhotos = corolla.getPhotos();
        assertThat(corollaPhotos).isNotEmpty();
        assertThat(corollaPhotos.size()).isEqualTo(5);
        assertThat(corollaPhotos.getFirst().getThumbnail()).isTrue();

        PhotoResponse photoResponse;
        for (int i = 0; i < corollaPhotos.size(); i++) {
            photoResponse = corollaPhotos.get(i);
            assertThat(photoResponse.getId()).isEqualTo((i + 1));
            assertThat(photoResponse.getUrl())
                    .isEqualTo("http://localhost:" + port + "/v1/cars/photos/corolla" + (i + 1) + ".jpg");

            if (i != 0) {
                assertThat(photoResponse.getThumbnail()).isFalse();
            }
        }

    }

    @Order(4)
    @Test
    @Sql(scripts = "/sql/init_one_car.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clean_cars.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("GET /v1/cars/1 returns a car with the given id without a list of photos")
    void findById_ReturnsCarWithoutPhotos_WhenSuccessful() {
        var responseEntity = testRestTemplate
                .exchange(URL + "/{id}", HttpMethod.GET, null, CarGetResponse.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        CarGetResponse corolla = responseEntity.getBody();
        assertThat(corolla).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(corolla.getId()).isEqualTo(1L);
        assertThat(corolla.getBrand()).isEqualTo("Toyota");
        assertThat(corolla.getModel()).isEqualTo("Corolla");
        assertThat(corolla.getType()).isEqualTo("SUV");
        assertThat(corolla.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000.00));
        assertThat(corolla.getYear()).isEqualTo(2022);

        assertThat(corolla.getPhotos()).isEmpty();
    }

    @Order(5)
    @Test
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

    @Order(6)
    @Test
    @Sql(scripts = "/sql/clean_cars.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("POST /v1/car creates a car")
    void save_CreatesCar_WhenSuccessful() throws IOException {
        var request = fileUtils.readResourceFile("car/post-request-car-200.json");
        var carEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL, HttpMethod.POST, carEntity, CarPostResponse.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CarPostResponse response = responseEntity.getBody();
        assertThat(response).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Order(7)
    @Test
    @DisplayName("POST /v1/car returns 403 when user is not admin")
    @Sql(scripts = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clean_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void save_ReturnsForbidden_WhenUserIsNotAdmin() throws Exception {
        var request = fileUtils.readResourceFile("car/post-request-car-200.json");
        var carEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.withBasicAuth("common_user", "123456")
                .exchange(URL, HttpMethod.POST, carEntity, String.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Order(8)
    @Test
    @DisplayName("POST /v1/car returns 400 when body is invalid")
    void save_ReturnsBadRequest_WhenBodyIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("car/post-request-car-400.json");
        var expectedResponse = fileUtils.readResourceFile("car/post-response-car-400.json");
        var carEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL, HttpMethod.POST, carEntity, String.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatJson(responseEntity.getBody()).whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }


    @Order(9)
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

    @Order(10)
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

    @Order(11)
    @Test
    @DisplayName("DELETE v1/car/1 removes a car when successful")
    @Sql(value = "/sql/init_one_car.sql")
    void delete_RemovesCar_WhenSuccessful() {
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/{id}", HttpMethod.DELETE, null, Void.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Order(12)
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
