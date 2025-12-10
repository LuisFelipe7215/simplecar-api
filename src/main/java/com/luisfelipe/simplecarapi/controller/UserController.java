package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.mapper.UserMapper;
import com.luisfelipe.simplecarapi.request.UserPostRequest;
import com.luisfelipe.simplecarapi.response.UserPostResponse;
import com.luisfelipe.simplecarapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    @PostMapping
    public ResponseEntity<UserPostResponse> save(@RequestBody UserPostRequest userPostRequest){
        log.debug("Request received to save user: '{}'", userPostRequest);

        var userToSave = mapper.toUser(userPostRequest);

        var savedUser = service.save(userToSave);

        var userPostResponse = mapper.toUserPostResponse(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(userPostResponse);
    }
}
