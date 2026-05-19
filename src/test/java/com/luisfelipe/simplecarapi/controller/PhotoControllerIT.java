package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.response.CarGetResponse;
import com.luisfelipe.simplecarapi.response.PhotoPostResponse;
import com.luisfelipe.simplecarapi.response.PhotoPutResponse;
import com.luisfelipe.simplecarapi.response.PhotoResponse;
import com.luisfelipe.simplecarapi.utils.FileUtils;
import com.luisfelipe.simplecarapi.utils.PhotoUtils;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/sql/init_one_admin_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class PhotoControllerIT {
    public static final String URL = "/v1/cars";
    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private PhotoUtils photoUtils;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Order(1)
    @Test
    @Sql(scripts = "/sql/init_one_car.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("POST /v1/cars/1/photos creates a new photo to a specific car when successful")
    void savePhoto_CreatesPhoto_WhenSuccessful() {
        MockMultipartFile mockFile = photoUtils.getMockFileToSave();

        var requestEntity = buildMockFIleHttpEntity(mockFile);

        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/{carId}/photos", HttpMethod.POST, requestEntity, PhotoPostResponse.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PhotoPostResponse response = responseEntity.getBody();
        assertThat(response).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getThumbnail()).isTrue();
        String fileName = response.getFileName().split("_")[1];
        assertThat(fileName).isEqualTo(mockFile.getOriginalFilename());
    }

    @Order(2)
    @Test
    @Sql(scripts = "/sql/init_one_car.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clean_cars.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("POST /v1/cars/99/photos throws NotFoundException 404 when car is not found")
    void savePhoto_ThrowsNotFoundException_WhenCarIsNotFound() throws IOException {
        MockMultipartFile mockFile = photoUtils.getMockFileToSave();

        var requestEntity = buildMockFIleHttpEntity(mockFile);

        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/{carID}/photos", HttpMethod.POST, requestEntity, String.class, 99);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();

        String expectedResponse = fileUtils.readResourceFile("photo/post-response-photo-404.json");

        JsonAssertions.assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }

    @Order(3)
    @Test
    @Sql(scripts = {"/sql/init_one_car.sql", "/sql/init_five_photos.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("POST /v1/cars/1/photos throws MaxPhotosExceededException 400 when car already has 5 photos")
    void savePhoto_ThrowsMaxPhotosExceededException_WhenCarHasMaxPhotos() throws IOException {
        MockMultipartFile mockFile = photoUtils.getMockFileToSave();

        var requestEntity = buildMockFIleHttpEntity(mockFile);

        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/{carID}/photos", HttpMethod.POST, requestEntity, String.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();

        String expectedResponse = fileUtils.readResourceFile("photo/post-response-photo-400.json");

        JsonAssertions.assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }

    @Order(4)
    @Test
    @DisplayName("POST /v1/car/1/photos returns forbidden 403 when user is not admin")
    @Sql(scripts = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void savePhoto_ReturnsForbidden_WhenUserIsNotAdmin() {
        MockMultipartFile mockFile = photoUtils.getMockFileToSave();

        var requestEntity = buildMockFIleHttpEntity(mockFile);

        var responseEntity = testRestTemplate.withBasicAuth("common_user", "123456")
                .exchange(URL + "/{carID}/photos", HttpMethod.POST, requestEntity, String.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Order(5)
    @Test
    @DisplayName("POST /v1/car/1/photos returns 401 when user is not authenticated")
    @Sql(scripts = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void savePhoto_ReturnsUnauthorized_WhenUserIsNotAuthenticated() {
        MockMultipartFile mockFile = photoUtils.getMockFileToSave();

        var requestEntity = buildMockFIleHttpEntity(mockFile);

        var responseEntity = testRestTemplate
                .exchange(URL + "/{carID}/photos", HttpMethod.POST, requestEntity, String.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Order(6)
    @Test
    @DisplayName("PUT /v1/car/photos/1 updates a photo when successful")
    @Sql(scripts = {"/sql/init_one_car.sql", "/sql/init_one_photo.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updatePhoto_UpdatesPhoto_WhenSuccessful() {
        MockMultipartFile mockFileToUpdate = photoUtils.getMockFileToUpdate();

        var requestEntity = buildMockFIleHttpEntity(mockFileToUpdate);

        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/photos/{id}", HttpMethod.PUT, requestEntity, PhotoPutResponse.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        PhotoPutResponse photoPutResponse = responseEntity.getBody();
        assertThat(photoPutResponse).isNotNull();

        assertThat(photoPutResponse.getId()).isEqualTo(1L);
        assertThat(photoPutResponse.getThumbnail()).isTrue();
        String fileName = photoPutResponse.getFileName().split("_")[1];
        assertThat(fileName).isEqualTo(mockFileToUpdate.getOriginalFilename());
    }

    @Order(7)
    @Test
    @DisplayName("PUT /v1/car/photos/1 preserves photos when successful")
    @Sql(scripts = {"/sql/init_one_car.sql", "/sql/init_one_photo.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updatePhoto_PreservesPhotos_WhenSuccessful() {
        MockMultipartFile mockFileToUpdate = photoUtils.getMockFileToUpdate();

        var requestEntity = buildMockFIleHttpEntity(mockFileToUpdate);

        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/photos/{id}", HttpMethod.PUT, requestEntity, PhotoPutResponse.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var getResponse = testRestTemplate
                .exchange(URL + "/{id}", HttpMethod.GET, null, CarGetResponse.class, 1);

        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();

        List<PhotoResponse> photos = getResponse.getBody().getPhotos();
        assertThat(photos).hasSize(1);
        String url = photos.getFirst().getUrl();
        String extractedFileName = url.substring(url.lastIndexOf('/') + 1).split("_")[1];

        String expectedFileName = "update-test.jpg";
        assertThat(extractedFileName).isEqualTo(expectedFileName);
    }

    @Order(8)
    @Test
    @DisplayName("PUT /v1/car/photos/99 throws NotfoundException 404 when photo is not found")
    @Sql(scripts = {"/sql/init_one_car.sql", "/sql/init_one_photo.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updatePhoto_ThrowsNotFoundException_WhenPhotoIsNotFound() throws IOException {
        MockMultipartFile mockFileToUpdate = photoUtils.getMockFileToUpdate();

        var requestEntity = buildMockFIleHttpEntity(mockFileToUpdate);

        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/photos/{id}", HttpMethod.PUT, requestEntity, String.class, 99);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();

        String expectedResponse = fileUtils.readResourceFile("photo/put-response-photo-404.json");
        JsonAssertions.assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }


    @Order(9)
    @Test
    @DisplayName("PUT /v1/car/photos/1 returns forbidden 403 when user is not admin")
    @Sql(scripts = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updatePhoto_ReturnsForbidden_WhenUserIsNotAdmin() {
        MockMultipartFile mockFileToUpdate = photoUtils.getMockFileToUpdate();

        var requestEntity = buildMockFIleHttpEntity(mockFileToUpdate);

        var responseEntity = testRestTemplate.withBasicAuth("common_user", "123456")
                .exchange(URL + "/photos/{id}", HttpMethod.PUT, requestEntity, String.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Order(10)
    @Test
    @DisplayName("PUT /v1/car/photos/1 returns 401 when user is not authenticated")
    @Sql(scripts = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updatePhoto_ReturnsUnauthorized_WhenUserIsNotAuthenticated() {
        MockMultipartFile mockFileToUpdate = photoUtils.getMockFileToUpdate();

        var requestEntity = buildMockFIleHttpEntity(mockFileToUpdate);

        var responseEntity = testRestTemplate
                .exchange(URL + "/photos/{id}", HttpMethod.PUT, requestEntity, String.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Order(11)
    @Test
    @DisplayName("DELETE /v1/car/photos/1 removes photo by its id")
    @Sql(scripts = {"/sql/init_one_car.sql", "/sql/init_one_photo.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deletePhoto_RemovesPhoto_WhenSuccessful() throws IOException {
        Path uploadDirPath = Paths.get(uploadDir);
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);
        }

        Files.write(uploadDirPath.resolve("corolla.jpg"), "test content".getBytes());

        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/photos/{id}", HttpMethod.DELETE, null, Void.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(Files.exists(uploadDirPath.resolve("corolla.jpg"))).isFalse();

        var getResponse = testRestTemplate
                .exchange(URL + "/{id}", HttpMethod.GET, null, CarGetResponse.class, 1);

        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        List<PhotoResponse> photos = getResponse.getBody().getPhotos();
        assertThat(photos).isEmpty();
    }


    @Order(12)
    @Test
    @DisplayName("DELETE /v1/car/photos/1 throws NotfoundException 404 when photo is not found")
    @Sql(scripts = {"/sql/init_one_car.sql", "/sql/init_one_photo.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/sql/clean_photos.sql", "/sql/clean_cars.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deletePhoto_ThrowsNotFoundException_WhenPhotoIsNotFound() throws IOException {
        var responseEntity = testRestTemplate.withBasicAuth(adminUsername, adminPassword)
                .exchange(URL + "/photos/{id}", HttpMethod.DELETE, null, String.class, 99);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        System.out.println(responseEntity.getBody());

        String expectedResponse = fileUtils.readResourceFile("photo/delete-response-photo-404.json");
        JsonAssertions.assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp")
                .isEqualTo(expectedResponse);
    }

    @Order(10)
    @Test
    @DisplayName("DELETE /v1/car/photos/1 returns forbidden 403 when user is not admin")
    @Sql(scripts = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deletePhoto_ReturnsForbidden_WhenUserIsNotAdmin() {
        var responseEntity = testRestTemplate.withBasicAuth("common_user", "123456")
                .exchange(URL + "/photos/{id}", HttpMethod.DELETE, null, String.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Order(11)
    @Test
    @DisplayName("DELETE /v1/car/photos/1 returns 401 when user is not authenticated")
    @Sql(scripts = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deletePhoto_ReturnsUnauthorized_WhenUserIsNotAuthenticated() {
        var responseEntity = testRestTemplate
                .exchange(URL + "/photos/{id}", HttpMethod.DELETE, null, String.class, 1);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static HttpEntity<MultiValueMap<String, Resource>> buildMockFIleHttpEntity(MockMultipartFile mockFile) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Resource> body = new LinkedMultiValueMap<>();
        body.add("file", mockFile.getResource());

        return new HttpEntity<>(body, httpHeaders);
    }


}