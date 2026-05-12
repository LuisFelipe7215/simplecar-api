package com.luisfelipe.simplecarapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class CustomNotFoundException {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
}
