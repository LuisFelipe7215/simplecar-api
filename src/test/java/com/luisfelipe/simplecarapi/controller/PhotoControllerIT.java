package com.luisfelipe.simplecarapi.controller;

import com.luisfelipe.simplecarapi.config.SecurityConfig;
import com.luisfelipe.simplecarapi.domain.Photo;
import com.luisfelipe.simplecarapi.exception.MaxPhotosExceededException;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.mapper.PhotoMapperImpl;
import com.luisfelipe.simplecarapi.service.PhotoService;
import com.luisfelipe.simplecarapi.utils.FileUtils;
import com.luisfelipe.simplecarapi.utils.PhotoUtils;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PhotoController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({PhotoService.class, PhotoMapperImpl.class, FileUtils.class, PhotoUtils.class, SecurityConfig.class})
@WithMockUser
class PhotoControllerTest {
    public static final String URL = "/v1/cars";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PhotoService photoService;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private PhotoUtils photoUtils;

    @Order(1)
    @Test
    @DisplayName("POST /v1/cars/1/photos creates a new photo to a specific car when successful")
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void savePhoto_CreatesPhoto_WhenSuccessful() throws Exception {
        Long carId = photoUtils.getCar().getId();

        Photo savedPhoto = photoUtils.getPhoto();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "teste.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes()
        );

        String response = fileUtils.readResourceFile("/photo/post-response-photo-201.json");

        BDDMockito.given(photoService.savePhoto(carId, mockFile)).willReturn(savedPhoto);

        mockMvc.perform(multipart(URL + "/{carId}/photos", carId)
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(response));
    }

    @Order(2)
    @Test
    @DisplayName("POST /v1/cars/99/photos throws NotFoundException 404 when car is not found")
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void savePhoto_ThrowsNotFoundException_WhenCarIsNotFound() throws Exception {
        Long carId = photoUtils.getCar().withId(99L).getId();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "teste.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes()
        );

        BDDMockito.given(photoService.savePhoto(carId, mockFile)).willThrow(new NotFoundException("Car not found"));

        mockMvc.perform(multipart(URL + "/{carId}/photos", carId)
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Order(3)
    @Test
    @DisplayName("POST /v1/cars/1/photos throws MaxPhotosExceededException 400 when car already has 5 photos")
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void savePhoto_ThrowsMaxPhotosExceededException_WhenCarHasMaxPhotos() throws Exception {
        Long carId = photoUtils.getCarWithMaxPhotos().getId();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "teste.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes()
        );

        BDDMockito.given(photoService.savePhoto(carId, mockFile))
                .willThrow(new MaxPhotosExceededException("Maximum number of photos (5) exceeded"));

        mockMvc.perform(multipart(URL + "/{carId}/photos", carId)
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Order(4)
    @Test
    @DisplayName("POST /v1/car/1/photos returns forbidden 403 when user is not admin")
    @WithMockUser
    void savePhoto_ReturnsForbidden_WhenUserIsNotAdmin() throws Exception {
        Long carId = photoUtils.getCar().getId();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "teste.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes()
        );

        mockMvc.perform(multipart(URL + "/{carId}/photos", carId)
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Order(5)
    @Test
    @DisplayName("PUT /v1/car/photos/1 updates a photo when successful")
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void updatePhoto_UpdatesPhoto_WhenSuccessful() throws Exception {
        Photo updatedPhoto = photoUtils.getPhotoToUpdate();
        Long photoId = updatedPhoto.getId();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "update_test.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes()
        );

        String response = fileUtils.readResourceFile("/photo/put-response-photo-200.json");

        BDDMockito.when(photoService.updatePhoto(photoId, mockFile)).thenReturn(updatedPhoto);

        mockMvc.perform(multipart(URL + "/photos/{id}", photoId)
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }

    @Order(6)
    @Test
    @DisplayName("PUT /v1/car/photos/99 throws NotfoundException 404 when photo is not found")
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void updatePhoto_ThrowsNotFoundException_WhenPhotoIsNotFound() throws Exception {
        Long photoId = photoUtils.getPhotoToUpdate().withId(99L).getId();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "update_test.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes()
        );

        BDDMockito.when(photoService.updatePhoto(photoId, mockFile)).thenThrow(new NotFoundException("Photo not found"));

        mockMvc.perform(multipart(URL + "/photos/{id}", photoId)
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Order(7)
    @Test
    @DisplayName("PUT /v1/car/photos/1 returns forbidden 403 when user is not admin")
    @WithMockUser
    void updatePhoto_ReturnsForbidden_WhenUserIsNotAdmin() throws Exception {
        Long photoId = photoUtils.getPhotoToUpdate().getId();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "teste.jpg", MediaType.IMAGE_JPEG_VALUE, "test content".getBytes()
        );

        mockMvc.perform(multipart(URL + "/photos/{id}", photoId)
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Order(8)
    @Test
    @DisplayName("DELETE /v1/car/photos/1 removes photo by its id")
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void deletePhoto_RemovesPhoto_WhenSuccessful() throws Exception {
        Long photoId = photoUtils.getPhotoToUpdate().getId();

        BDDMockito.willDoNothing().given(photoService).deletePhoto(photoId);

        mockMvc.perform(delete(URL + "/photos/{id}", photoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Order(9)
    @Test
    @DisplayName("DELETE /v1/car/photos/1 throws NotfoundException 404 when photo is not found")
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void deletePhoto_ThrowsNotFoundException_WhenPhotoIsNotFound() throws Exception {
        Long photoId = photoUtils.getPhotoToUpdate().withId(99L).getId();

        BDDMockito.willThrow(new NotFoundException("Photo not found")).given(photoService).deletePhoto(photoId);

        mockMvc.perform(delete(URL + "/photos/{id}", photoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Order(10)
    @Test
    @DisplayName("DELETE /v1/car/photos/1 returns forbidden 403 when user is not admin")
    @WithMockUser()
    void deletePhoto_ReturnsForbidden_WhenUserIsNotAdmin() throws Exception {
        Long photoId = photoUtils.getPhotoToUpdate().getId();

        BDDMockito.willThrow(new NotFoundException("Photo not found")).given(photoService).deletePhoto(photoId);

        mockMvc.perform(delete(URL + "/photos/{id}", photoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


}