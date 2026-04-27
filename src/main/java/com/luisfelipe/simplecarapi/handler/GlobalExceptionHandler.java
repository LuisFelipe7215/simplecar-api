package com.luisfelipe.simplecarapi.handler;

import com.luisfelipe.simplecarapi.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomNotFoundException> handleNotFoundException(NotFoundException e
            , HttpServletRequest request) {
        CustomNotFoundException error = new CustomNotFoundException(HttpStatus.NOT_FOUND.value(),
                e.getReason(),
                LocalDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MaxPhotosExceededException.class)
    public ResponseEntity<CustomMaxPhotosExceededException> handleMaxPhotosExceededException(MaxPhotosExceededException e
            , HttpServletRequest request) {
        CustomMaxPhotosExceededException error = new CustomMaxPhotosExceededException(HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
