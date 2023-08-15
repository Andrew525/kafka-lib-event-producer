package com.kafkalibevent.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafkalibevent.example.domain.LibraryEvent;
import com.kafkalibevent.example.domain.LibraryEventType;
import com.kafkalibevent.example.producer.LibraryEventProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class LibraryEventControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlerException(MethodArgumentNotValidException ex) {

        var errMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + " - " + fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        log.info("MethodArgumentNotValidException: {}", errMsg);
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(errMsg);
    }


}
