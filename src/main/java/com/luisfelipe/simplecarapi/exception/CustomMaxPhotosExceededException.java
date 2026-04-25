package com.luisfelipe.simplecarapi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomMaxPhotosExceededException {
    private Integer status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
}
