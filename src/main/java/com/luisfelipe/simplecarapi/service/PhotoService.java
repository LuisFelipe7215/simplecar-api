package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.repository.CarRepository;
import com.luisfelipe.simplecarapi.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final CarRepository carRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public Photo savePhoto(Long carId, MultipartFile file) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new NotFoundException("Car not found with id: " + carId));

        String fileName = storeFile(file);

        boolean isFirstPhoto = car.getPhotos() == null || car.getPhotos().isEmpty();

        Photo photo = Photo.builder()
                .fileName(fileName)
                .car(car)
                .thumbnail(isFirstPhoto)
                .build();

        return photoRepository.save(photo);
    }

    private String storeFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }
}
