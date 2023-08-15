package com.kafkalibevent.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkalibevent.example.domain.LibraryEvent;
import com.kafkalibevent.example.util.TestUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
class LibraryEventControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;


    @BeforeEach
    void setUp() {
        var props = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", kafkaBroker));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(props, new IntegerDeserializer(), new StringDeserializer())
                .createConsumer();
        kafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void postLibraryEvent() {
        //given
        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        var httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), httpHeaders);

        //when
        var responseEntity = restTemplate.exchange(
                "/v1/libraryevents", HttpMethod.POST, httpEntity, LibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        var records = KafkaTestUtils.getRecords(consumer);
        assertEquals(1, records.count());

        records.forEach(it -> {
            var libraryEvent = TestUtil.parseLibraryEventRecord(objectMapper, it.value());

            assertEquals(TestUtil.libraryEventRecord(), libraryEvent);
        });

    }
}