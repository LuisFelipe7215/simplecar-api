package com.luisfelipe.simplecarapi.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Default response model to Username already exists exception")
@AllArgsConstructor
@Getter
@Setter
public class CustomUsernameAlreadyExistsException {
    @Schema(description = "Http status code", example = "409")
    private int status;
    @Schema(description = "Error detailed message", example = "Username already exists")
    private String message;
    @Schema(description = "Error date and time", example = "2025-12-13T10:30:00")
    private LocalDateTime timestamp;
    @Schema(description = "Request path", example = "/v1/users")
    private String path;
}
