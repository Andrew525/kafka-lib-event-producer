package com.kafkalibevent.example.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkalibevent.example.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class LibraryEventProducer {

    KafkaTemplate<Integer, String> kafkaTemplate;
    ObjectMapper objectMapper;

    @Value("${spring.kafka.template.default-topic}")
    String topic;

    public LibraryEventProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        // 1. blocking call — getting kafka cluster metadata -- only once in the beginning
        // 2. send massage happens — return CompletableFuture
        kafkaTemplate.send(topic, key, value)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        handleFailure(key, value, throwable);
                    } else {
                        handleSuccess(key, value, result);
                    }
                });
    }

    public void sendLibraryEventV2_syncCall(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        // 1. blocking call — getting kafka cluster metadata -- only once in the beginning
        // 2. and wait till we get result
        kafkaTemplate.send(topic, key, value)
                .get(3, TimeUnit.SECONDS);
    }

    public void sendLibraryEventV3_producerRecord(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        var producerRecord = producerRecord(key, value);

        // 1. blocking call — getting kafka cluster metadata -- only once in the beginning
        // 2. and wait till we get result
        kafkaTemplate.send(producerRecord)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        handleFailure(key, value, throwable);
                    } else {
                        handleSuccess(key, value, result);
                    }
                });

    }

    private ProducerRecord<Integer, String> producerRecord(Integer key, String value) {
        List<Header> headers = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, null, key, value, headers);
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message Sent SuccessFully for the key : " +
                "{} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }
}

