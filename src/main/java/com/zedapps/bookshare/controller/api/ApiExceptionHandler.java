package com.zedapps.bookshare.controller.api;

import com.zedapps.bookshare.dto.api.ErrorResponseDto;
import jakarta.persistence.NoResultException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * @author smzoha
 * @since 20/4/26
 **/
@RestControllerAdvice(basePackages = "com.zedapps.bookshare.controller.api")
public class ApiExceptionHandler {

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResult(NoResultException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(List.of("error.not.found")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDto(List.of("error.invalid.request")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(List.of("error.internal")));
    }
}
