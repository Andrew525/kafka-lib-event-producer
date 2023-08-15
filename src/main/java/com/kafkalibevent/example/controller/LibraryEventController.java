package com.kafkalibevent.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafkalibevent.example.domain.LibraryEvent;
import com.kafkalibevent.example.domain.LibraryEventType;
import com.kafkalibevent.example.producer.LibraryEventProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("/v1")
public class LibraryEventController {

    LibraryEventProducer producer;

    public LibraryEventController(LibraryEventProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/libraryevents")
    public ResponseEntity<?> createEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        log.debug("POST request to create new libraryEvent with body: {}", libraryEvent);
        if (LibraryEventType.NEW != libraryEvent.libraryEventType()) {
            log.debug("WRONG libraryEventType. It should be NEW, but provided: {}", libraryEvent.libraryEventType());
            return ResponseEntity.status(BAD_REQUEST).body("Only NEW event type is supported");
        }
        producer.sendLibraryEventV3_producerRecord(libraryEvent);
        log.debug("Created libraryEvent: {}", libraryEvent);
        return ResponseEntity.status(CREATED).body(libraryEvent);
    }


    @PutMapping("/libraryevents")
    public ResponseEntity<?> updateEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        log.debug("PUT request to create new libraryEvent with body: {}", libraryEvent);

        ResponseEntity<String> BAD_REQUEST = validateLibraryEvent(libraryEvent);
        if (BAD_REQUEST != null) {
            return BAD_REQUEST;
        }

        producer.sendLibraryEvent(libraryEvent);
        log.debug("Created libraryEvent: {}", libraryEvent);
        return ResponseEntity.status(OK).body(libraryEvent);
    }

    private static ResponseEntity<String> validateLibraryEvent(LibraryEvent libraryEvent) {
        if (libraryEvent.libraryEventId() == null) {
            log.debug("Missing libraryEventId");
            return ResponseEntity.status(BAD_REQUEST).body("Missing libraryEventId");
        }

        if (LibraryEventType.UPDATE != libraryEvent.libraryEventType()) {
            log.debug("Inside the if block");
            return ResponseEntity.status(BAD_REQUEST).body("Only UPDATE event type is supported");
        }
        return null;
    }


}
