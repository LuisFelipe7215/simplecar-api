package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.exception.MaxPhotosExceededException;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.repository.CarRepository;
import com.luisfelipe.simplecarapi.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final CarRepository carRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public Photo savePhoto(Long carId, MultipartFile file) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new NotFoundException("Car not found with id: " + carId));

        if (car.getPhotos() != null && car.getPhotos().size() >= 5) {
            throw new MaxPhotosExceededException("Maximum number of photos (5) exceeded for car with id: " + carId);
        }

        String fileName = storeFile(file);

        boolean isFirstPhoto = car.getPhotos() == null || car.getPhotos().isEmpty();

        Photo photo = Photo.builder()
                .fileName(fileName)
                .car(car)
                .thumbnail(isFirstPhoto)
                .build();

        return photoRepository.save(photo);
    }
    
    @Transactional
    public Photo updatePhoto(Long id, MultipartFile file){
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Photo not found with id: " + id));
        
        deleteFile(photo.getFileName());

        String newFileName = storeFile(file);
        
        photo.setFileName(newFileName);
        
        return photoRepository.save(photo);
    }

    @Transactional
    public void deletePhoto(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Photo not found with id: " + id));

        deleteFile(photo.getFileName());

        photoRepository.delete(photo);

        log.debug("Deleted photo with id: {}", id);
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

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", fileName);
        } catch (IOException e) {
            log.error("Error deleting file: {}", fileName, e);
        }
    }
}
