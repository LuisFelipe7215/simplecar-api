package com.luisfelipe.simplecarapi.handler;

import com.luisfelipe.simplecarapi.exception.CustomNotFoundException;
import com.luisfelipe.simplecarapi.exception.CustomUsernameAlreadyExistsException;
import com.luisfelipe.simplecarapi.exception.NotFoundException;
import com.luisfelipe.simplecarapi.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<CustomUsernameAlreadyExistsException> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e
            , HttpServletRequest request) {
        CustomUsernameAlreadyExistsException error = new CustomUsernameAlreadyExistsException(HttpStatus.CONFLICT.value(),
                e.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

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
}
