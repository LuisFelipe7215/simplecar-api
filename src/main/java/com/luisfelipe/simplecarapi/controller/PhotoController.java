package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.mapper.PhotoMapper;
import com.luisfelipe.simplecarapi.response.PhotoPostResponse;
import com.luisfelipe.simplecarapi.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/cars")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService service;
    private final PhotoMapper mapper;

    @PostMapping("/{carId}/photos")
    public ResponseEntity<PhotoPostResponse> uploadPhoto(@PathVariable Long carId, @RequestParam("file") MultipartFile file) {
        Photo savedPhoto = service.savePhoto(carId, file);
        PhotoPostResponse response = mapper.toPhotoPostResponse(savedPhoto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
