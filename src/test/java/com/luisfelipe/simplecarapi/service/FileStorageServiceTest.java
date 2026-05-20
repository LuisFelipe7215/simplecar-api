package com.luisfelipe.simplecarapi.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Order(1)
    @Test
    @DisplayName("storeFile stores file successfully when called with valid MultipartFile")
    void storeFile_StoresFileSuccessfully_WhenValidMultipartFile() throws IOException {
        String originalFileName = "test_image.jpg";
        String fileContent = "test image content.";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFileName,
                MediaType.IMAGE_JPEG_VALUE,
                fileContent.getBytes()
        );

        String storedFileName = fileStorageService.storeFile(mockMultipartFile);

        assertThat(storedFileName).isNotNull().contains(originalFileName);
        Path storedFilePath = tempDir.resolve(storedFileName);
        assertThat(Files.exists(storedFilePath)).isTrue();
        assertThat(Files.readAllBytes(storedFilePath)).isEqualTo(fileContent.getBytes());
    }

    @Order(2)
    @Test
    @DisplayName("storeFile throws RuntimeException when file storage fails due to IOException")
    void storeFile_ThrowsRuntimeException_WhenIOExceptionOccurs() {
        String originalFileName = "bad_file.txt";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFileName,
                MediaType.TEXT_PLAIN_VALUE,
                "some content".getBytes()
        );

        tempDir.toFile().setReadOnly();

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> fileStorageService.storeFile(mockMultipartFile))
                .withMessageContaining("Could not store the file.");

        tempDir.toFile().setWritable(true);
    }

    @Order(3)
    @Test
    @DisplayName("deleteFile deletes file successfully when file exists")
    void deleteFile_DeletesFileSuccessfully_WhenFileExists() {
        String originalFileName = "file_to_delete.jpg";
        String fileContent = "Content to be deleted.";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFileName,
                MediaType.IMAGE_JPEG_VALUE,
                fileContent.getBytes()
        );
        String storedFileName = fileStorageService.storeFile(mockMultipartFile);
        Path storedFilePath = tempDir.resolve(storedFileName);

        assertThat(Files.exists(storedFilePath)).isTrue();

        fileStorageService.deleteFile(storedFileName);

        assertThat(Files.exists(storedFilePath)).isFalse();
    }

    @Order(4)
    @Test
    @DisplayName("deleteFile does nothing when file does not exist")
    void deleteFile_DoesNothing_WhenFileDoesNotExist() {
        String nonExistentFileName = "non_existent_file.jpg";
        Assertions.assertThatNoException().isThrownBy(() -> fileStorageService.deleteFile(nonExistentFileName));
    }

    @Order(5)
    @Test
    @DisplayName("storeFile creates necessary directories if they don't exist based on uploadDir structure")
    void storeFile_CreatesDirectories_WhenUploadDirHasSubPaths() throws IOException {
        Path subDirPath = tempDir.resolve("sub_dir").resolve("another_sub_dir");
        FileStorageService serviceWithSubDir = new FileStorageService(subDirPath.toString());

        String originalFileName = "deep_file.png";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFileName,
                MediaType.IMAGE_PNG_VALUE,
                "deep content".getBytes()
        );

        String storedFileName = serviceWithSubDir.storeFile(mockMultipartFile);

        Path expectedFilePath = subDirPath.resolve(storedFileName);
        assertThat(Files.exists(expectedFilePath)).isTrue();
        assertThat(Files.isDirectory(subDirPath)).isTrue();
        assertThat(Files.readAllBytes(expectedFilePath)).isEqualTo("deep content".getBytes());
    }
}
