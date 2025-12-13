package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.utils.FileUtils;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIT {
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private FileUtils fileUtils;

    @Order(1)
    @Test
    @DisplayName("POST /v1/users 201 creates a new user when successful")
    void save_CreatesUser_WhenSuccessful() throws IOException {
        var request = fileUtils.readResourceFile("user/post-request-user-200.json");
        var response = fileUtils.readResourceFile("user/post-response-user-201.json");
        var userEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.exchange("/v1/users", HttpMethod.POST, userEntity, String.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonAssertions.assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("id")
                .isEqualTo(response);
    }

    @Order(2)
    @Test
    @DisplayName("POST /v1/users throws UsernameAlreadyExistsException 409 when username already exists.")
    @Sql(value = "/sql/init_one_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void save_ThrowsUsernameAlreadyExistsException_WhenUsernameExists() throws IOException {
        var request = fileUtils.readResourceFile("user/post-request-user-409.json");
        var expectedResponse = fileUtils.readResourceFile("user/post-response-user-409.json");
        var userEntity = buildHttpEntity(request);
        var responseEntity = testRestTemplate.exchange("/v1/users", HttpMethod.POST, userEntity, String.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        JsonAssertions.assertThatJson(responseEntity.getBody())
                .whenIgnoringPaths("timestamp", "detail")
                .isEqualTo(expectedResponse);
    }

    private static HttpEntity<String> buildHttpEntity(String request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, httpHeaders);
    }
}