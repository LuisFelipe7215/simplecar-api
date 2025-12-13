package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.exception.CustomUsernameAlreadyExistsException;
import com.luisfelipe.simplecarapi.mapper.UserMapper;
import com.luisfelipe.simplecarapi.request.UserPostRequest;
import com.luisfelipe.simplecarapi.response.UserPostResponse;
import com.luisfelipe.simplecarapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/users")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "User API", description = "User api related endpoints")
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    @PostMapping
    @Operation(summary = "Save a new user with role 'USER'",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request to save a new user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserPostRequest.class))),
            responses = {
                    @ApiResponse(description = "New user is saved",
                            responseCode = "201",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserPostResponse.class))),
                    @ApiResponse(description = "Username already exists",
                            responseCode = "409",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CustomUsernameAlreadyExistsException.class)))
            })
    public ResponseEntity<UserPostResponse> save(@RequestBody UserPostRequest userPostRequest) {
        log.debug("Request received to save user: '{}'", userPostRequest);

        var userToSave = mapper.toUser(userPostRequest);

        var savedUser = service.save(userToSave);

        var userPostResponse = mapper.toUserPostResponse(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(userPostResponse);
    }
}
