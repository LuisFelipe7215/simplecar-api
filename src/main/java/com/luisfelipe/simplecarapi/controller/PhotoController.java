package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.mapper.PhotoMapper;
import com.luisfelipe.simplecarapi.response.PhotoPostResponse;
import com.luisfelipe.simplecarapi.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1/cars")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService service;
    private final PhotoMapper mapper;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/photos/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename){
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()){
                return ResponseEntity.ok().body(resource);
            } else{
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{carId}/photos")
    public ResponseEntity<PhotoPostResponse> uploadPhoto(@PathVariable Long carId, @RequestParam("file") MultipartFile file) {
        Photo savedPhoto = service.savePhoto(carId, file);
        PhotoPostResponse response = mapper.toPhotoPostResponse(savedPhoto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
