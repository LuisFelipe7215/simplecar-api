package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.Car;
import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.exception.MaxPhotosExceededException;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.repository.CarRepository;
import com.luisfelipe.simplecarapi.repository.PhotoRepository;
import com.luisfelipe.simplecarapi.utils.PhotoUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PhotoServiceTest {
    @InjectMocks
    private PhotoService photoService;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private FileStorageService fileStorageService;
    @InjectMocks
    private PhotoUtils photoUtils;


    @Order(1)
    @Test
    @DisplayName("Save photo creates a new photo to a specific car when successful")
    void savePhoto_CreatesPhoto_WhenSuccessful() {
        Car car = photoUtils.getCar();
        MultipartFile mockFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes()
        );

        Photo expectedPhoto = photoUtils.getPhoto(car);
        String expectedFileName = expectedPhoto.getFileName();

        BDDMockito.given(carRepository.findById(anyLong())).willReturn(Optional.of(car));
        BDDMockito.given(fileStorageService.storeFile(any(MultipartFile.class))).willReturn(expectedFileName);
        BDDMockito.given(photoRepository.save(any(Photo.class))).willReturn(expectedPhoto);

        Photo savedPhoto = photoService.savePhoto(car.getId(), mockFile);

        Assertions.assertThat(savedPhoto).isNotNull();
        Assertions.assertThat(savedPhoto.getId()).isEqualTo(1L);
        Assertions.assertThat(savedPhoto.getFileName()).isEqualTo(expectedFileName);
        Assertions.assertThat(savedPhoto.getCar().getId()).isEqualTo(car.getId());
        Assertions.assertThat(savedPhoto.getThumbnail()).isTrue();

        BDDMockito.verify(carRepository).findById(car.getId());
        BDDMockito.verify(fileStorageService).storeFile(mockFile);
        BDDMockito.verify(photoRepository).save(any(Photo.class));
    }

    @Order(2)
    @Test
    @DisplayName("Save photo throws NotFoundException when car is not found")
    void savePhoto_ThrowsNotFoundException_WhenCarIsNotFound() {
        BDDMockito.given(carRepository.findById(anyLong())).willReturn(Optional.empty());
        MultipartFile mockFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes()
        );

        Assertions.assertThatThrownBy(() -> photoService.savePhoto(1L, mockFile))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Car not found");

        BDDMockito.verify(carRepository).findById(anyLong());
        BDDMockito.verifyNoInteractions(fileStorageService);
        BDDMockito.verifyNoInteractions(photoRepository);
    }

    @Order(3)
    @Test
    @DisplayName("Save photo throws MaxPhotosExceededException when car already has 5 photos")
    void savePhoto_ThrowsMaxPhotosExceededException_WhenCarHasMaxPhotos() {
        Car carWithMaxPhotos = photoUtils.getCarWithMaxPhotos();
        BDDMockito.given(carRepository.findById(anyLong())).willReturn(Optional.of(carWithMaxPhotos));
        MultipartFile mockFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes()
        );

        Assertions.assertThatThrownBy(() -> photoService.savePhoto(carWithMaxPhotos.getId(), mockFile))
                .isInstanceOf(MaxPhotosExceededException.class)
                .hasMessageContaining("Maximum number of photos (5) exceeded");

        BDDMockito.verify(carRepository).findById(anyLong());
        BDDMockito.verifyNoInteractions(fileStorageService);
        BDDMockito.verifyNoInteractions(photoRepository);
    }

    @Order(4)
    @Test
    @DisplayName("Update updates a photo when successful")
    void updatePhoto_UpdatesPhoto_WhenSuccessful() {
        Car car = photoUtils.getCar();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "update_test.jpg", "image/jpeg", "test content".getBytes()
        );
        String updatedFilename = mockFile.getOriginalFilename();
        Photo photoToUpdate = photoUtils.getPhoto(car);

        BDDMockito.given(photoRepository.findById(photoToUpdate.getId())).willReturn(Optional.of(photoToUpdate));
        BDDMockito.willDoNothing().given(fileStorageService).deleteFile(photoToUpdate.getFileName());
        BDDMockito.given(fileStorageService.storeFile(mockFile)).willReturn(updatedFilename);

        Photo photoWithNewFileName = photoToUpdate.withFileName(updatedFilename);
        BDDMockito.given(photoRepository.save(photoWithNewFileName)).willReturn(photoWithNewFileName);

        Photo updatedPhoto = photoService.updatePhoto(photoToUpdate.getId(), mockFile);

        Assertions.assertThat(updatedPhoto).isNotNull();
        Assertions.assertThat(updatedPhoto.getId()).isEqualTo(photoToUpdate.getId());
        Assertions.assertThat(updatedPhoto.getFileName()).isEqualTo(updatedFilename);
        Assertions.assertThat(updatedPhoto.getCar().getId()).isEqualTo(car.getId());
        Assertions.assertThat(updatedPhoto.getThumbnail()).isTrue();

        BDDMockito.verify(photoRepository).findById(photoToUpdate.getId());
        BDDMockito.verify(fileStorageService).storeFile(mockFile);
        BDDMockito.verify(photoRepository).save(photoWithNewFileName);
    }

    @Order(5)
    @Test
    @DisplayName("Update throws NotfoundException when photo is not found")
    void updatePhoto_ThrowsNotFoundException_WhenPhotoIsNotFound() {
        Car car = photoUtils.getCar();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "update_test.jpg", "image/jpeg", "test content".getBytes()
        );
        Photo photoToUpdate = photoUtils.getPhoto(car).withId(99L);
        Long id = photoToUpdate.getId();

        BDDMockito.given(photoRepository.findById(id)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> photoService.updatePhoto(id, mockFile))
                        .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Photo not found with id: " + id);


        BDDMockito.verify(photoRepository).findById(id);
        BDDMockito.verifyNoInteractions(fileStorageService);
    }

    @Order(6)
    @Test
    @DisplayName("Delete removes photo by its id")
    void deletePhoto_RemovesPhoto_WhenSuccessful() {
        Car car = photoUtils.getCar();
        Photo photoToDelete = photoUtils.getPhoto(car).withId(1L);
        Long id = photoToDelete.getId();

        BDDMockito.given(photoRepository.findById(id)).willReturn(Optional.of(photoToDelete));
        BDDMockito.willDoNothing().given(fileStorageService).deleteFile(photoToDelete.getFileName());
        BDDMockito.willDoNothing().given(photoRepository).delete(photoToDelete);

        Assertions.assertThatNoException()
                .isThrownBy(() -> photoService.deletePhoto(id));

        BDDMockito.verify(photoRepository).findById(id);
    }

    @Order(7)
    @Test
    @DisplayName("Delete throws NotfoundException when photo is not found")
    void deletePhoto_ThrowsNotFoundException_WhenPhotoIsNotFound() {
        Car car = photoUtils.getCar();
        Photo photoToDelete = photoUtils.getPhoto(car).withId(99L);
        Long id = photoToDelete.getId();

        BDDMockito.given(photoRepository.findById(id)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> photoService.deletePhoto(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Photo not found with id: " + id);

        BDDMockito.verify(photoRepository).findById(id);
        BDDMockito.verifyNoInteractions(fileStorageService);
    }
}